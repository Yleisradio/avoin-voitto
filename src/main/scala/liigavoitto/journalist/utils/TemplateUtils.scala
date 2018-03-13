package liigavoitto.journalist.utils

import liigavoitto.journalist.values.MatchDataValues
import liigavoitto.scores.{FeedPlayer, Player, Team}
import liigavoitto.util.Logging
import scaledn.{EDNKeyword, EDNSymbol, EDNValue}
import scaledn.parser.parseEDN

import scala.io.Source
import scala.util.{Failure, Success, Try}

trait TemplateUtils extends RenderUtils with LiigaAttributeValuesUtils

trait RenderUtils extends Logging {
  def render(template: String, attr: Map[String, Any]): Option[String] = {
    Try {
      Mustache(template).apply(attr)
    } match {
      case Success(s) => Some(s)
      case Failure(e) =>
        log.warn(s"Could not render '$template': " + e.getMessage)
        None
    }
  }

  def render(template: Template, attr: Map[String, Any]): Option[RenderedTemplate] = {
    TemplateRendering.render(template, attr)
  }
}

trait DeclensionUtils {
  type GeneralDeclensions = Map[String, Map[EDNKeyword, String]]
  type GeneralLangDeclensions = Map[String, Map[EDNKeyword, Map[EDNKeyword, String]]]
  type LangDeclensions = Map[EDNKeyword, Map[EDNKeyword, Map[EDNKeyword, String]]]

  lazy val teamDeclensions = parseEDN(loadResource("declension/teams.edn")).get.asInstanceOf[GeneralLangDeclensions]
  lazy val playerDeclensions = parseEDN(loadResource("declension/players.edn")).get.asInstanceOf[GeneralDeclensions]
  lazy val helperDeclensions = parseEDN(loadResource("declension/helpers.edn")).get.asInstanceOf[LangDeclensions]
  lazy val numeralDeclensions = parseEDN(loadResource("declension/numerals.edn")).get.asInstanceOf[LangDeclensions]

  def getTeamDeclension(teamName: String, declension: String, lang: String) = {
    val langKw = EDNKeyword(EDNSymbol(lang))
    val declensionKw = EDNKeyword(EDNSymbol(declension))
    Try(teamDeclensions(teamName)(langKw)(declensionKw)).toOption
  }

  def getPlayerDeclension(playerLastName: String, declension: String) = {
    val declensionKw = EDNKeyword(EDNSymbol(declension))
    Try(playerDeclensions(playerLastName)(declensionKw)).toOption
  }

  def getHelperDeclension(helperId: String, declension: String, lang: String) = {
    val helperKw = EDNKeyword(EDNSymbol(helperId))
    val langKw = EDNKeyword(EDNSymbol(lang))
    val declensionKw = EDNKeyword(EDNSymbol(declension))
    Try(helperDeclensions(helperKw)(langKw)(declensionKw)).toOption
  }

  def getNumeralDeclension(number: String, declension: String, lang: String) = {
    val numeralKw = EDNKeyword(EDNSymbol(number))
    val langKw = EDNKeyword(EDNSymbol(lang))
    val declensionKw = EDNKeyword(EDNSymbol(declension))
    Try(numeralDeclensions(numeralKw)(langKw)(declensionKw)).toOption
  }

  private def loadResource(path: String) = {
    val resourcePath =  path
    val res = getClass.getClassLoader.getResource(resourcePath)
    val source = Source.fromURL(res)
    source.mkString
  }
}

trait LiigaAttributeUtils extends DeclensionUtils {
  def teamWithDeclensions(base: String, team: Team, lang: String): Map[String, Any] =
    Map(base -> team.name) ++ teamDeclensions(base, team, lang)
  def teamWithDeclensions(base: String, team: Option[Team], lang: String): Map[String, Any] =
    team.map(t => teamWithDeclensions(base, t, lang)).getOrElse(Map())

  private def teamDeclensions(base: String, team: Team, lang: String): Map[String, Any] = {
    Declension.values.flatMap(decl =>
      getTeamDeclension(team.name, decl.toString, lang)
        .map(res => s"$base:$decl" -> res)
    ).toMap
  }

  def feedPlayerWithDeclensions(base: String, player: Option[FeedPlayer]): Map[String, Any] =
    player.map(feedPlayerWithDeclensions(base, _)).getOrElse(Map())
  def feedPlayerWithDeclensions(base: String, player: FeedPlayer) = Map(
    base -> (player.name.first + " " + player.name.last),
    s"$base.last" -> player.name.last
  ) ++ feedPlayerDeclensions(base, player)

  def playerWithDeclensions(base: String, player: Option[Player]): Map[String, Any] =
    player.map(playerWithDeclensions(base, _)).getOrElse(Map())
  def playerWithDeclensions(base: String, player: Player) = Map(
    base -> (player.name.first + " " + player.name.last),
    s"$base.last" -> player.name.last
  ) ++ playerDeclensions(base, player)

  private def playerDeclensions(base: String, player: Player): Map[String, Any] = {
    playerDeclensionsWithName(base, player.name.first, player.name.last)
  }

  private def feedPlayerDeclensions(base: String, player: FeedPlayer): Map[String, Any] = {
    playerDeclensionsWithName(base, player.name.first, player.name.last)
  }

  private def playerDeclensionsWithName(base: String, firstName: String, lastName: String) : Map[String, Any] = {
    Declension.values.flatMap(decl =>
      getPlayerDeclension(lastName, decl.toString)
        .map(res => List(s"$base:$decl" -> s"$firstName $res", s"$base.last:$decl" -> res))
    ).flatten.toMap
  }

  def helperDeclensions(base: String, helper: String, lang: String): Map[String, Any] = {
    Declension.values.flatMap(decl =>
      getHelperDeclension(helper, decl.toString, lang)
        .map(res => s"$base:$decl" -> res)
    ).toMap
  }

  def numeralWithDeclensions(base: String, number: Int, lang: String): Map[String, String] = Map(
    base -> number.toString
  ) ++ numeralDeclensions(base, number, lang)

  def numeralWithDeclensions(base: String, number: Option[Int], lang: String): Map[String, String] =
    number.map(numeralWithDeclensions(base, _, lang)).getOrElse(Map())

  private def numeralDeclensions(base: String, number: Int, lang: String): Map[String, String] = {
    Declension.values.flatMap(decl =>
      getNumeralDeclension(s"number-$number", decl.toString, lang)
        .map(res => s"$base:$decl" -> res)
    ).toMap
  }

  def playerTemplateAttributes(base: String, player: Player, lang: String): Map[String, Any] =
    playerStatsTemplateAttributes(base, Stats.getFrom(player), lang) ++ playerWithDeclensions(base, player)
  def playerTemplateAttributes(base: String, player: Option[Player], lang: String): Map[String, Any] =
    player.map(p => playerTemplateAttributes(base, p, lang)).getOrElse(Map())

  def playerStatsTemplateAttributes(base: String, player: Player, lang: String): Map[String, String] = {
    playerStatsTemplateAttributes(base, Stats.getFrom(player), lang)
  }

  def goalieStatsTemplateAttributes(base: String, player: Option[Player], lang: String): Map[String, String] =
    player.map(goalieStatsTemplateAttributes(base, _, lang)).getOrElse(Map())
  def goalieStatsTemplateAttributes(base: String, player: Player, lang: String): Map[String, String] = {
    val stats = Stats.getGoalieStats(player)
    val statsFirstPeriod = Stats.getPerPeriod(player).get("1")
    withCapitalized(
      numeralWithDeclensions(s"$base.saves", stats.saves, lang) ++
      numeralWithDeclensions(s"$base.firstPeriod.saves", statsFirstPeriod.map(_.saves), lang)
    )
  }

  def playerStatsTemplateAttributes(base: String, stats: Stats, lang: String): Map[String, String] = withCapitalized(
   numeralWithDeclensions(s"$base.goals", stats.goals, lang) ++
   numeralWithDeclensions(s"$base.assists", stats.assists, lang) ++
   numeralWithDeclensions(s"$base.points", stats.points, lang) ++
   Map(s"$base.statsText" -> playerStatsText(stats)))

  def withCapitalized(attributes: Map[String, String]) =
    attributes ++ attributes.map(kv => kv._1 + ":capitalize" -> kv._2.capitalize)

  private def playerStatsText(stats: Stats) = {
    stats.goals + "+" + stats.assists
  }
}

trait LiigaAttributeValuesUtils extends LiigaAttributeUtils {
  implicit val values: MatchDataValues

  def teamAndOpponentWithDeclensions(base: String, value: Team, lang: String) = {
    teamWithDeclensions(base, value, lang) ++ teamWithDeclensions(s"$base.opponent", values.otherTeam(value), lang)
  }

  def teamAndOpponentWithDeclensions(base: String, value: Option[Team], lang: String) = {
    teamWithDeclensions(base, value, lang) ++ teamWithDeclensions(s"$base.opponent", values.otherTeam(value), lang)
  }
}
