package liigavoitto.journalist.events

import liigavoitto.journalist.MatchData
import liigavoitto.journalist.text.CommonImplicits
import liigavoitto.journalist.values.MatchDataValues
import liigavoitto.scores.{Feed, FeedPlayer, Match, Team}
import liigavoitto.transform.TextBlock

import scala.collection.mutable.ListBuffer

case class GameEvent(`type`: String, time: String, player: Option[EventPlayer], team: Team, standing: Option[String] = None, goalType: Option[String] = None, assists: Option[List[EventPlayer]] = None, timeInMins: Option[String] = None, penaltyText: Option[String] = None, changedGoaltender: Option[EventPlayer] = None, goaltenderPull: Boolean = false, goaltenderReturn: Boolean = false, goaltenderChange: Boolean = false)
case class EventPlayer(name: String)

class GameEventsGenerator(matchData: MatchData, language: String) extends CommonImplicits {
  val lang = language
  val mtch = matchData.mtch
  val values = MatchDataValues(matchData, lang)

  private val filePath = "template/events/event-texts.edn"
  private def tmpl = getTemplateFn(filePath)
  private val penaltyFilePath = "template/events/penalties.edn"
  private def penaltyTmpl = getTemplateFn(penaltyFilePath)
  private val goalTypesFilePath = "template/events/goal-types.edn"
  private def goalTypesTmpl = getTemplateFn(goalTypesFilePath)

  private lazy val overtimeText = tmpl("overtime").head.template
  private lazy val shootoutText = tmpl("shootout").head.template

  def scoresList = scores.map(score =>
    GameEvent(
      `type`    = "score",
      time      = zeroPad(score.gameTime.get),
      player    = Some(eventPlayer(score.player.get)),
      team      = score.team.get,
      standing  = Some(standing(score)),
      goalType  = score.goalType,
      assists   = Some(mapPlayers(assistsForScore(score)))
    )
  )
  def penaltiesList = penalties.filter(hasPenaltyPlayer).map(penalty =>
    GameEvent(
      `type`      = "penalty",
      time        = zeroPad(penalty.gameTime.get),
      player      = Some(eventPlayer(findPenaltyPlayer(penalty))),
      team        = findPenaltyTeam(penalty),
      timeInMins  = Some(mins(penalty)),
      penaltyText = penalty.text
    )
  )
  def goalkeepingList = {
    goalkeepings.filter(_.gameTime.get != "00:00").map(goalkeeping => {
      GameEvent(
        `type`            = "goalkeeping",
        time              = zeroPad(goalkeeping.gameTime.get),
        player            = findGoaltenderOut(goalkeeping),
        team              = findGoaltenderTeam(goalkeeping),
        changedGoaltender = findGoaltenderIn(goalkeeping),
        goaltenderPull    = isGoaltenderPull(goalkeeping),
        goaltenderReturn  = isGoaltenderReturn(goalkeeping),
        goaltenderChange  = isGoaltenderChange(goalkeeping)
      )
    })
  }

  def gameEvents = (penaltiesList ++ scoresList ++ goalkeepingList).sortBy(p => values.gameTimeInSeconds(p.time))

  def getEventContentBlocks: List[TextBlock] = List(
      TextBlock(getFirstPeriodEventString, "text"),
      TextBlock(getSecondPeriodEventString, "text"),
      TextBlock(getThirdPeriodEventString, "text")
    ) ++ getOptionalEventList.flatten


  def getEventsStringForTimePeriod(from: Int, to: Int): String =
    gameEvents.filter(p => from <= values.gameTimeInSeconds(p.time) && values.gameTimeInSeconds(p.time) < to).flatMap(ge => ge.`type` match {
      case "score" => Some(scoreText(ge).getOrElse("") + assistsText(ge).getOrElse("") ++ goalTypeText(ge).getOrElse(""))
      case "penalty" => penaltyText(ge)
      case "goalkeeping" => goalkeepingText(ge)
    }).mkString("   \n")
  def getEventsStringForTimePeriod(from: String, to: String): String =
    getEventsStringForTimePeriod(values.gameTimeInSeconds(from), values.gameTimeInSeconds(to))

  def getFirstPeriodEventString: String = s"**${getPeriodText(1)}**   \n" + getEventsStringForTimePeriod("00:00", "20:00")

  def getSecondPeriodEventString: String = s"**${getPeriodText(2)}**   \n" + getEventsStringForTimePeriod("20:00", "40:00")

  def getThirdPeriodEventString: String = s"**${getPeriodText(3)}**   \n" + getEventsStringForTimePeriod("40:00", "60:00")

  def getPeriodText(period: Int) = {
    val text = render(tmpl("period").head.template, numeralWithDeclensions("periodNumber", period, lang)).get

    if (lang == "sv") text.capitalize
    else text
  }

  def getAdditionalPeriodText(period: Int) = {
    val text = render(tmpl("additional-period").head.template, numeralWithDeclensions("periodNumber", period, lang)).get

    if (lang == "sv") text.capitalize
    else text
  }

  def getOptionalEventList: List[Option[TextBlock]] = {
    if (values.isPlayoffs) playoffAdditionalPeriods
    else List(getOvertimeEventOptionString().map(p => TextBlock(p, "text")),
      getShootoutEventOptionString().map(p => TextBlock(p, "text")))
  }

  def playoffAdditionalPeriods: List[Option[TextBlock]] = {
    if (!values.hasAdditionalPeriods) List(None)
    else List.range(1, values.lastAdditionalPeriodNumber.get + 1).map(i => Some(TextBlock(getPlayoffAdditionalPeriodString(i), "text")))
  }

  def getPlayoffAdditionalPeriodString(period: Int): String = {
    val periodStart = 60 * 60 + (period - 1) * 20 * 60
    val periodEnd = 60 * 60 + period * 20 * 60
    s"**${getAdditionalPeriodText(period)}**   \n" + getEventsStringForTimePeriod(periodStart, periodEnd)
  }


  def getOvertimeEventOptionString(): Option[String] = {
    val overTimeString = getEventsStringForTimePeriod("60:00", "65:00")
    // Make sure JATKOAIKA string is there if we go into shootouts even if nothing happened during it
    val shootoutString = getEventsStringForTimePeriod("65:00", "66:00")
    if (overTimeString.length > 0 || shootoutString.length > 0) Some(s"**$overtimeText**   \n" + overTimeString)
    else None
  }

  def getShootoutEventOptionString(): Option[String] = {
    val shootoutString = getEventsStringForTimePeriod("65:00", "66:00")
    if (shootoutString.length > 0) Some(s"**$shootoutText**   \n" + shootoutString)
    else None
  }

  private def scores = mtch.feed.filter(_.`type` == "score")
  private def noScores = mtch.feed.filter(_.`type` == "noScore")
  private def assists = mtch.feed.filter(_.`type` == "assist")
  private def penalties = mtch.feed.filter(_.`type` == "penalty")
  private def penaltySufferers = mtch.feed.filter(_.`type` == "penaltysufferer")
  private def goalkeepings = mtch.feed.filter(_.`type` == "goalkeeping")

  private def scoreText(g: GameEvent): Option[String] = Some(s"**${g.time} ${g.standing.getOrElse("")} ${name(g.player.get)}**")
  private def assistsText(g: GameEvent): Option[String] = if (g.assists.get.length > 0) Some(" (" + g.assists.get.map(name).mkString(", ") + ")") else None
  private def goalTypeText(g: GameEvent): Option[String] = Some(s" ${goalTypeTranslation(g.goalType.getOrElse(""))}")
  private def penaltyText(g: GameEvent): Option[String] = Some(s"${g.time} ${g.timeInMins.get} ${name(g.player.get)} (${team(g.team)}) - ${penaltyTranslation(g.penaltyText.getOrElse(""))}")
  private def goalkeepingText(g: GameEvent): Option[String] = {
    if (g.goaltenderChange) {
      render(tmpl("goalie-change").head.template, Map("time" -> g.time, "goaltenderIn" -> name(g.changedGoaltender.get), "goaltenderOut" -> name(g.player.get), "team" -> team(g.team)))
    } else if (g.goaltenderPull) {
      render(tmpl("goalie-pull").head.template, Map("time" -> g.time, "goaltenderOut" -> name(g.player.get), "team" -> team(g.team)))
    } else if (g.goaltenderReturn) {
      render(tmpl("goalie-return").head.template, Map("time" -> g.time, "goaltenderIn" -> name(g.changedGoaltender.get), "team" -> team(g.team)))
    } else None
  }

  private def name(player: FeedPlayer) = shorten(player.name.first) + " " + player.name.last
  private def shorten(firstName: String) = if (firstName.length > 2) firstName.head + "." else firstName
  private def assistsForScore(score: Feed) = assists.filter(_.gameTime == score.gameTime)
  private def mapPlayers(feed: List[Feed]) = feed.map(f => eventPlayer(f.player.get))
  private def zeroPad(time: String): String = if (time.length < 5) zeroPad("0" + time) else time
  private def eventPlayer(player: FeedPlayer) = EventPlayer(name(player))
  private def standing(score: Feed): String = values.withCurrentScore(score, values.scoresBefore(score))._2.productIterator.mkString("-")
  private def name(player: EventPlayer) = player.name
  private def team(team: Team) = team.name
  private def mins(penalty: Feed): String = if (isPenaltyShot(penalty)) penaltyTmpl("penalty-shot").head.template else s"${penalty.timeInMins.getOrElse("0")} min"

  private def penaltyTranslation(text: String) = {
    val template = text match {
      case "Laitataklaus" => penaltyTmpl("boarding")
      case "Laitataklaus - Pelirangaistus" => penaltyTmpl("boarding-gm")
      case "Epäurheilijamainen käytös" => penaltyTmpl("unsportsmanlike-behaviour")
      case "Epäurheilijamainen käytös - Pelirangaistus" => penaltyTmpl("unsportsmanlike-behaviour-gm")
      case "Epäurheilijamainen käytös - Käytösrangaistus" => penaltyTmpl("unsportsmanlike-behaviour-m")
      case "Väkivaltaisuus" => penaltyTmpl("roughing")
      case "Väkivaltaisuus - Pelirangaistus" => penaltyTmpl("roughing-gm")
      case "Kiinnipitäminen" => penaltyTmpl("holding")
      case "Kiinnipitäminen - Käytösrangaistus" => penaltyTmpl("holding-m")
      case "Mailasta kiinnipitäminen" => penaltyTmpl("holding-the-stick")
      case "Poikittainen maila" => penaltyTmpl("cross-checking")
      case "Pelin viivyttäminen - kiekko katsomoon" => penaltyTmpl("delay-of-game")
      case "Pelin viivyttäminen - kiekon sulkeminen" => penaltyTmpl("delay-of-game-closing-hand-on-puck")
      case "Pelin viivyttäminen - rike aloitustapahtumassa" => penaltyTmpl("delay-of-game-foul-in-start-event")
      case "Pelin viivyttäminen - maalin siirtäminen" => penaltyTmpl("delay-of-game-moving-goal")
      case "Huitominen" => penaltyTmpl("slashing")
      case "Pään tai niskan alueelle kohdistuva taklaus" => penaltyTmpl("check-to-the-head")
      case "Pään tai niskan alueelle kohdistuva taklaus - Pelirangaistus" => penaltyTmpl("check-to-the-head-gm")
      case "Pään tai niskan alueelle kohdistuva taklaus - Käytösrangaistus" => penaltyTmpl("check-to-the-head-m")
      case "Liian monta pelaajaa jäällä" => penaltyTmpl("too-many-men-on-the-ice")
      case "Estäminen" => penaltyTmpl("interference")
      case "Estäminen - Pelirangaistus" => penaltyTmpl("interference-gm")
      case "Maalivahdin estäminen" => penaltyTmpl("goalie-interference")
      case "Korkea maila" => penaltyTmpl("high-sticking")
      case "Korkea maila - Pelirangaistus" => penaltyTmpl("high-sticking-gm")
      case "Selästä taklaaminen" => penaltyTmpl("checking-from-behind")
      case "Selästä taklaaminen - Pelirangaistus" => penaltyTmpl("checking-from-behind-gm")
      case "Selästä taklaaminen - Käytösrangaistus" => penaltyTmpl("checking-from-behind-m")
      case "Kampitus" => penaltyTmpl("tripping")
      case "Kampitus - Pelirangaistus" => penaltyTmpl("tripping-gm")
      case "Koukkaaminen" => penaltyTmpl("hooking")
      case "Keihästäminen" => penaltyTmpl("spearing")
      case "Keihästäminen - Pelirangaistus" => penaltyTmpl("spearing-gm")
      case "Tappelu" => penaltyTmpl("fighting")
      case "Mailan heitto" => penaltyTmpl("throwing-the-stick")
      case "Sukeltaminen" => penaltyTmpl("diving")
      case "Kyynärpäätaklaus" => penaltyTmpl("elbowing")
      case "Kiekon sulkeminen" => penaltyTmpl("closing-hand-on-puck")
      case "Kiekon sulkeminen käteen" => penaltyTmpl("closing-hand-on-puck")
      case "Polvitaklaus" => penaltyTmpl("kneeing")
      case "Polvitaklaus - Pelirangaistus" => penaltyTmpl("kneeing-gm")
      case "Leikkaaminen" => penaltyTmpl("clipping")
      case "Mailan päällä lyöminen" => penaltyTmpl("spearing-stick-end")
      case "Rikkoutunut maila" => penaltyTmpl("broken-stick")
      case "Väärä varuste" => penaltyTmpl("illegal-equipment")
      case "Päällä iskeminen" => penaltyTmpl("head-butting")
      case "Jalkapyyhkäisy" => penaltyTmpl("slew-footing")
      case "Ryntäys" => penaltyTmpl("charging")
      case "Ryntäys - Pelirangaistus" => penaltyTmpl("charging-gm")
      case "Potkaiseminen" => penaltyTmpl("kicking")
      case "Mailan tai muun esineen heitto" => penaltyTmpl("throwing-stick-or-object")
      case "Mailan tai muun esineen heitto - Pelirangaistus" => penaltyTmpl("throwing-stick-or-object-gm")
      case "Sopimaton käytös tuomaristoa kohtaan - Käytösrangaistus" => penaltyTmpl("unsportsmanlike-behaviour-arguing-with-referee-m")
      case "Sopimaton käytös tuomaristoa kohtaan - Pelirangaistus" => penaltyTmpl("unsportsmanlike-behaviour-arguing-with-referee-gm")
      case _ => List()
    }
    val translation = template.map(_.template)
    // if no translation available, use the default.
    if (translation.length > 0) translation.head else text
  }

  private def goalTypeTranslation(text: String) = {
    text.split(",").map(p => singleGoalTypeTranslation(p)).mkString(",")
  }

  private def singleGoalTypeTranslation(text: String) = {
    val template = text match {
      case "YV" => goalTypesTmpl("YV")
      case "YV2" => goalTypesTmpl("YV2")
      case "AV" => goalTypesTmpl("AV")
      case "TM" => goalTypesTmpl("TM")
      case "IM" => goalTypesTmpl("IM")
      case "VT" => goalTypesTmpl("VT")
      case "VL" => goalTypesTmpl("VL")
      case "RL" => goalTypesTmpl("RL")
      case "TV" => goalTypesTmpl("TV")
      case "SR" => goalTypesTmpl("SR")
      case _ => List()
    }
    val translation = template.map(_.template)
    // if no translation available, use the default.
    if (translation.nonEmpty) translation.head else text
  }

  private def hasPenaltyPlayer(penalty: Feed) = penalty.player.isDefined || findPenaltySufferer(penalty).map(_.player.get).isDefined
  private def findPenaltyPlayer(penalty: Feed) = penalty.player.getOrElse(findPenaltySufferer(penalty).map(_.player.get).get)
  private def findPenaltySufferer(feed: Feed) = penaltySufferers.find(ps => ps.gameTime == feed.gameTime)
  private def findPenaltyTeam(penalty: Feed) = penalty.team.getOrElse(findPenaltySufferer(penalty).map(_.team.get).get)

  private def findGoaltenderTeam(goalkeeping: Feed): Team = {
    // If goalie is pulled, no player information present. Use the team that is losing.
    // There might be very rare cases when goalie is pulled in lead, like last match before playoffs and team needs better goal difference.
    if (isGoaltenderPull(goalkeeping)) getPulledGoaltenderTeam(goalkeeping)
    else goalkeeping.team.get
  }
  private def isGoaltenderPull(goalkeeping: Feed): Boolean = (goalkeeping.text.isDefined && goalkeeping.text.get == "Maalivahti pois")

  private def isGoaltenderReturn(goalkeeping: Feed): Boolean = (!isGoaltenderPull(goalkeeping) && goalkeeping.gameTime.get == goalkeeping.beginTime.get && goalkeepings.exists(gk => isGoaltenderPull(gk) && gk.endTime.get == goalkeeping.beginTime.get))

  private def isGoaltenderChange(goalkeeping: Feed): Boolean = {
    if (!isGoaltenderPull(goalkeeping)) {
      goalkeepings.exists(gk => (!gk.text.isDefined && gk.endTime.get == goalkeeping.beginTime.get && gk.team.get.id == goalkeeping.team.get.id && gk.player.get.id != goalkeeping.player.get.id))
    }
    else false
  }

  private def getPulledGoaltenderTeam(goalkeeping: Feed): Team = {
    val leaving = goalkeepings.filter(gk => gk.endTime.get == goalkeeping.gameTime.get)
    if (leaving.length > 1) {
      val score = values.scoreAtTime(goalkeeping.gameTime.get)
      if (score.home > score.away) values.away else values.home
    }
    else leaving.head.team.get
  }

  private def findGoaltenderIn(goalkeeping: Feed): Option[EventPlayer] = {
    if (!isGoaltenderPull(goalkeeping)) {
      Some(eventPlayer(goalkeepings.find(gk => gk.beginTime.get == goalkeeping.gameTime.get && gk.team.isDefined && gk.team.get.id == goalkeeping.team.get.id).get.player.get))
    }
    else None
  }

  private def findGoaltenderOut(goalkeeping: Feed): Option[EventPlayer] = {
    if (isGoaltenderChange(goalkeeping) || isGoaltenderPull(goalkeeping)) {
      val team = findGoaltenderTeam(goalkeeping)
      Some(eventPlayer(goalkeepings.find(gk => gk.endTime.get == goalkeeping.gameTime.get && gk.team.get.id == team.id).get.player.get))
    }
    else None
  }

  // If penalty minutes is 0 and same game time has score or noScore, it means that penalty was penalty shot.
  private def isPenaltyShot(penalty: Feed) = (penalty.timeInMins.get == "0" && scoredSameTimeAsPenalty(penalty))
  private def scoresAndNoScores = scores ++ noScores
  private def scoredSameTimeAsPenalty(penalty: Feed) = scoresAndNoScores.exists(s => s.gameTime == penalty.gameTime)
}
