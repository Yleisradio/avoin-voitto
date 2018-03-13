package liigavoitto.journalist.values

import liigavoitto.journalist.MatchData
import liigavoitto.journalist.utils.Stats
import liigavoitto.scores.{Feed, Match, Team, Outcome}

object StageEnum extends Enumeration {
  val RegularSeason, Playoffs, Training, Other = Value
}

case class MatchDataValues(md: MatchData, language: String)
  extends ImportantPlayerValues
    with LeagueTableValues
    with DateValues
    with GameProgressValues
    with PlayoffValues {

  object Stage extends Enumeration {
    val RegularSeason, Playoffs, TrainingGames, Pitsiturnaus, Other = Value
  }

  val lang = language
  val data = md
  val seriesId = data.seriesId
  val seriesValues = new SeriesValues(seriesId)
  val season = data.season
  val stage = mapStage(data.stage)
  val mtch = data.mtch
  val home = mtch.teams(0)
  val away = mtch.teams(1)
  val playerStats = data.playerStats

  val winner = if (home.score.get.now > away.score.get.now) home else away
  val loser = if (home.score.get.now > away.score.get.now) away else home
  val homeTeamWin = winner.abbr == home.abbr
  val endPeriod = winner.score.get.periods.length
  val totalGoals = winner.score.get.now + loser.score.get.now
  val shutout = loser.score.get.now == 0
  val result = s"${home.score.get.now}-${away.score.get.now}"
  val winnerResult = s"${winner.score.get.now}-${loser.score.get.now}"
  val loserResult = s"${loser.score.get.now}-${winner.score.get.now}"
  val goalDif = winner.score.get.now - loser.score.get.now
  val winningTeamPlayers = winner.players

  val homeShotsOnGoal = shotsOnGoal(home)
  val awayShotsOnGoal = shotsOnGoal(away)
  val homePenaltyMinutesGrouped = penaltyMinutesGrouped(home)
  val awayPenaltyMinutesGrouped = penaltyMinutesGrouped(away)
  val homePenaltyMinutesTotal = penaltyMinutesTotal(homePenaltyMinutesGrouped)
  val awayPenaltyMinutesTotal = penaltyMinutesTotal(awayPenaltyMinutesGrouped)
  val homeFaceOffWins = faceOffWins(home)
  val homeFaceOffLosses = faceOffLosses(home)
  val homeFaceOffPercentage = homeFaceOffWins.toDouble / (homeFaceOffWins + homeFaceOffLosses) * 100
  val awayFaceOffWins = faceOffWins(away)
  val awayFaceOffLosses = faceOffLosses(away)
  val awayFaceOffPercentage = awayFaceOffWins.toDouble / (awayFaceOffWins + awayFaceOffLosses) * 100

  val allHomeTeamMatches = data.allHomeTeamMatches.filter(m => m.status == "finished")
  val allAwayTeamMatches = data.allAwayTeamMatches.filter(m => m.status == "finished")
  val previousHomeTeamMatches = withoutCurrent(allHomeTeamMatches)
  val previousAwayTeamMatches = withoutCurrent(allAwayTeamMatches)
  val previousWinnerMatches = if (homeTeamWin) previousHomeTeamMatches else previousAwayTeamMatches
  val previousLoserMatches = if (homeTeamWin) previousAwayTeamMatches else previousHomeTeamMatches

  val homeTeamWins = wins(previousHomeTeamMatches, home) + 1
  val awayTeamWins = wins(previousAwayTeamMatches, away) + 1
  val homeTeamLosses = losses(previousHomeTeamMatches, home)
  val awayTeamLosses = losses(previousAwayTeamMatches, away)
  val homeTeamGames = previousHomeTeamMatches.length + 1
  val awayTeamGames = previousAwayTeamMatches.length + 1
  val winnerWins = if (homeTeamWin) homeTeamWins else awayTeamWins
  val winnerTotalMatches = if (homeTeamWin) homeTeamGames else awayTeamGames
  val loserLosses = if (!homeTeamWin) homeTeamLosses + 1 else awayTeamLosses + 1

  val winnerWinPercentage = if (homeTeamWin) (homeTeamWins).toDouble / homeTeamGames * 100 else (awayTeamWins).toDouble / awayTeamGames * 100
  val loserLossPercentage = if (!homeTeamWin) (homeTeamLosses).toDouble / awayTeamGames * 100 else (awayTeamLosses).toDouble / awayTeamGames * 100

  val headToHeadMatches = previousHomeTeamMatches.filter(_.teams.exists(_.id == away.id)) :+ mtch
  val headToHeadMatchesTotal = headToHeadMatches.length
  val homeTeamHeadToHeadWins = headToHeadMatches.count(teamWon(_, home))
  val awayTeamHeadToHeadWins = headToHeadMatches.count(teamWon(_, away))
  val headToHeadResult = if (homeTeamHeadToHeadWins > awayTeamHeadToHeadWins) s"$homeTeamHeadToHeadWins-$awayTeamHeadToHeadWins" else s"$awayTeamHeadToHeadWins-$homeTeamHeadToHeadWins"

  // streak values doesn't include current match.
  val winnerWinStreak = winStreak(previousWinnerMatches, winner)
  val winnerLossStreak = lossStreak(previousWinnerMatches, winner)
  val loserWinStreak = winStreak(previousLoserMatches, loser)
  val loserLossStreak = lossStreak(previousLoserMatches, loser)

  val attendance = mtch.stats.attendance
  val venue = home.meta.directives.map(_.getOrElse("homeArena", "-")).getOrElse("-")

  lazy val winningGoal = getWinningGoal
  lazy val winningGoalTime = winningGoal.gameTime.get

  lazy val isRegularSeason = stage == StageEnum.RegularSeason
  lazy val isPlayoffs = stage == StageEnum.Playoffs
  lazy val isTraining = stage == StageEnum.Training
  lazy val isOther = stage == StageEnum.Other

  // not in use yet.
  val significantStandingsRise = false
  val standingsPosition = 0
  val standingsPointsToFirst = 0

  def winnerPoints(howManyMatches: Int) = {
    points(previousWinnerMatches, winner).reverse.take(howManyMatches).sum
  }

  private def winningTeam(m: Match) = {
    m.teams.find(_.score.get.outcome match {
      case Outcome(1, 0, 0, 0, 0) => true // wins
      case Outcome(0, 0, 0, 1, 0) => true // otWins
      case _ => false
    }).get
  }

  private def teamWon(m: Match, t: Team) = winningTeam(m).id == t.id

  private def withoutCurrent(matches: List[Match]) = matches.filterNot(_.id == mtch.id)

  private def winStreak(matches: List[Match], team: Team) = {
    val result = outcomes(matches, team).map({
      case Outcome(1, 0, 0, 0, 0) => true // wins
      case Outcome(0, 0, 0, 1, 0) => true // otWins
      case _ => false
    })
    result.reverse.takeWhile(_ == true).length
  }

  private def lossStreak(matches: List[Match], team: Team) = {
    val result = outcomes(matches, team).map({
      case Outcome(0, 0, 1, 0, 0) => true // losses
      case Outcome(0, 0, 0, 0, 1) => true // otLosses
      case _ => false
    })
    result.reverse.takeWhile(_ == true).length
  }

  def wins(matches: List[Match], team: Team) = {
    outcomes(matches, team).map({
      case Outcome(1, 0, 0, 0, 0) => true // wins
      case Outcome(0, 0, 0, 1, 0) => true // otWins
      case _ => false
    }).count(_ == true)
  }

  def losses(matches: List[Match], team: Team) = {
    outcomes(matches, team).map({
      case Outcome(0, 0, 1, 0, 0) => true // losses
      case Outcome(0, 0, 0, 0, 1) => true // otLosses
      case _ => false
    }).count(_ == true)
  }

  private def points(matches: List[Match], team: Team) = {
    outcomes(matches, team).map({
      case Outcome(1, 0, 0, 0, 0) => 3 // wins
      case Outcome(0, 1, 0, 0, 0) => 1 // draws
      case Outcome(0, 0, 1, 0, 0) => 0 // losses
      case Outcome(0, 0, 0, 1, 0) => 2 // otWins
      case Outcome(0, 0, 0, 0, 1) => 1 // otLosses
      case _ => 0
    })
  }

  private def outcomes(matches: List[Match], team: Team) = {
    matches.map(m => m.teams.find(_.id == team.id).get.score.get.outcome)
  }

  private def teamFeed(team: Team) = mtch.feed.filter(_.team match {
    case Some(s) => s.id == team.id
    case None => false
  })

  private def filterScores(feed: List[Feed]) = feed.filter(_.`type` == "score")

  private def getWinningGoalRegulationTime = {
    Some(filterScores(teamFeed(winner)).reverse(goalDif - 1))
  }

  private def getWinningGoal: Feed = {
    if (endPeriod == 4 || endPeriod == 5) mtch.feed.reverse.find(_.`type` == "score").get
    else getWinningGoalRegulationTime.get
  }

  private def shotsOnGoal(team: Team): Int = {
    team.players.map(Stats.getFrom).map(s => s.shotsOnGoal).sum
  }

  private def penaltyMinutesTotal(minutes: Map[String, Int]): Int = minutes.map(m => m._1.toInt * m._2).sum

  private def penaltyMinutesGrouped(team: Team): Map[String, Int] = {
    val penalties = mtch.feed.filter(_.`type` == "penalty")
    val sufferers = mtch.feed.filter(_.`type` == "penaltysufferer")

    penalties.map(p => p.team match {
      case Some(t: Team) => if (t.id == team.id) p.timeInMins else None
      case None => if (sufferers.exists(s => s.gameTime == p.gameTime && s.team.get.id == team.id)) p.timeInMins else None
    }).flatten.groupBy(identity).mapValues(_.size)
  }

  private def faceOffWins(team: Team): Int = {
    team.players.map(Stats.getFrom).map(s => s.faceOffWins).sum
  }

  private def faceOffLosses(team: Team): Int = {
    team.players.map(Stats.getFrom).map(s => s.faceOffLosses).sum
  }

  private def mapStage(stage: String) = {
    val regularSeason = seriesValues.regularSeasonStageId.getOrElse(0)
    val playoffs = seriesValues.playoffsStageId.getOrElse(0)
    val training = seriesValues.trainingStageId.getOrElse(0)

    stage match {
      case `regularSeason` => StageEnum.RegularSeason
      case `playoffs` => StageEnum.Playoffs
      case `training` => StageEnum.Training
      case _ => StageEnum.Other
    }
  }
}
