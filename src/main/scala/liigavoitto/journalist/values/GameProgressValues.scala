package liigavoitto.journalist.values

import liigavoitto.journalist.utils.Stats
import liigavoitto.scores._

trait GameProgressValues {
  implicit val mtch: Match
  implicit val winner: Team
  implicit val loser: Team
  implicit val home: Team
  implicit val away: Team
  implicit val allPlayers: List[Player]

  lazy val homeFirstPeriodGoalDiff = firstPeriodScore(home) - firstPeriodScore(away)
  lazy val awayFirstPeriodGoalDiff = firstPeriodScore(away) - firstPeriodScore(home)
  lazy val secondPeriodGoalDiff = Math.abs(homeSecondPeriodGoalDiff)
  lazy val homeSecondPeriodGoalDiff = secondPeriodScore(home) - secondPeriodScore(away)
  lazy val awaySecondPeriodGoalDiff = secondPeriodScore(away) - secondPeriodScore(home)
  lazy val loserSecondPeriodGoalDiff = secondPeriodScore(loser) - secondPeriodScore(winner)

  lazy val tiedAfterFirstPeriod = homeFirstPeriodGoalDiff == 0
  lazy val tiedAfterFirstPeriodWithGoals = tiedAfterFirstPeriod && (firstPeriodScore(home) + firstPeriodScore(away) > 0)
  lazy val firstPeriodResult = firstPeriodScore(home) + "-" + firstPeriodScore(away)
  lazy val firstPeriodTotalGoals = firstPeriodScore(home) + firstPeriodScore(away)
  lazy val secondPeriodResult = secondPeriodScore(home) + "-" + secondPeriodScore(away)
  lazy val secondPeriodTotalGoals = goalsInSecondPeriod(home) + goalsInSecondPeriod(away)
  lazy val totalGoalsInThirdPeriod = goalsInThirdPeriod(home) + goalsInThirdPeriod(away)

  lazy val totalGoalsAfterSecondPeriod = secondPeriodScore(home) + secondPeriodScore(away)
  lazy val goalSpreeInThirdPeriod = totalGoalsInThirdPeriod >= 4 && totalGoalsInThirdPeriod >= totalGoalsAfterSecondPeriod
  lazy val tiedInFirstPeriodAndTakeoverInSecond = tiedAfterFirstPeriod && secondPeriodGoalDiff >= 1
  
  private def firstPeriodScore(team: Team) = team.score.get.periods.find(_.id == "1").get.score
  private def secondPeriodScore(team: Team) = firstPeriodScore(team) + goalsInSecondPeriod(team)
  private def thirdPeriodScore(team: Team) = secondPeriodScore(team) + goalsInThirdPeriod(team)

  private def goalsInSecondPeriod(team: Team) = team.score.get.periods.find(_.id == "2").get.score
  private def goalsInThirdPeriod(team: Team) = team.score.get.periods.find(_.id == "3").get.score

  lazy val firstGoal = mtch.feed.find(_.`type` == "score").head
  lazy val firstGoalPeriod = firstGoal.period.get
  lazy val firstGoalTime = firstGoal.gameTime.get
  lazy val firstGoalTimeMinutes = firstGoalTime.split(":").head.toInt
  lazy val firstGoalType = firstGoal.goalType.getOrElse("")
  lazy val firstGoalWithinTwoMinutes = firstGoalTimeMinutes < 2
  lazy val firstGoalLate = firstGoalTimeMinutes >= 15
  lazy val firstGoalAfterFifteenMinutesInFirstPeriod = firstGoalLate && firstGoalPeriod == "1"
  lazy val firstGoalInSecondPeriod = firstGoalPeriod == "2"
  lazy val firstGoalTeam = mtch.teams.find(_.id == firstGoal.team.get.id).get
  lazy val firstGoalTeamIsHome = firstGoalTeam.id == home.id
  lazy val firstGoalPlayer = firstGoalTeam.players.find(_.id == firstGoal.player.get.id).get
  lazy val firstGoalInOvertime = firstGoal.period.get == "4"
  lazy val firstGoalInShootout = firstGoal.period.get == "5"
  lazy val firstGoalInAdditionalPeriod = hasAdditionalPeriods && firstGoal.period.get.toInt > 3
  lazy val firstGoalAssists: List[FeedPlayer] = assistsForScore(firstGoal).map(_.player.get)
  lazy val firstGoalFirstAssistPlayer = firstGoalAssists.headOption
  lazy val firstGoalSecondAssistPlayer = firstGoalAssists.lift(1)

  lazy val firstPeriodWithoutGoals = firstPeriodTotalGoals == 0
  lazy val firstPeriodEqualSaves = firstPeriodHomeGoalieSaves.exists(s => s > 1) && firstPeriodHomeGoalieSaves == firstPeriodAwayGoalieSaves
  lazy val firstPeriodGoalieWithMostSavesTeam = if (firstPeriodHomeGoalieSaves.getOrElse(0) > firstPeriodAwayGoalieSaves.getOrElse(0)) home else away
  lazy val firstPeriodGoalieWithSecondMostSavesTeam = otherTeam(firstPeriodGoalieWithMostSavesTeam)
  lazy val firstPeriodGoalieWithMostSaves = goalieForPeriod(firstPeriodGoalieWithMostSavesTeam, "1").flatMap(findPlayerInTeam(_, firstPeriodGoalieWithMostSavesTeam))
  lazy val firstPeriodGoalieWithSecondMostSaves = goalieForPeriod(firstPeriodGoalieWithSecondMostSavesTeam, "1").flatMap(findPlayerInTeam(_, firstPeriodGoalieWithSecondMostSavesTeam))

  lazy val firstPeriodHomeGoalieSaves = goalieSavesByPeriod(home, 1)
  lazy val firstPeriodAwayGoalieSaves = goalieSavesByPeriod(away, 1)
  lazy val secondPeriodHomeGoalieSaves = goalieSavesByPeriod(home, 2)
  lazy val secondPeriodAwayGoalieSaves = goalieSavesByPeriod(away, 2)
  lazy val thirdPeriodHomeGoalieSaves = goalieSavesByPeriod(home, 3)
  lazy val thirdPeriodAwayGoalieSaves = goalieSavesByPeriod(away, 3)
  lazy val overtimeHomeGoalieSaves = goalieSavesByPeriod(home, 4)
  lazy val overtimeAwayGoalieSaves = goalieSavesByPeriod(away, 4)
  lazy val shootoutHomeGoalieSaves = goalieSavesByPeriod(home, 5)
  lazy val shootoutAwayGoalieSaves = goalieSavesByPeriod(away, 5)
  lazy val homeGoalieSavesCombinedByPeriod = combineGoalieSaves(home)
  lazy val awayGoalieSavesCombinedByPeriod = combineGoalieSaves(away)
  lazy val homeGoalieSavesTotal = homeGoalieSavesCombinedByPeriod.sum
  lazy val awayGoalieSavesTotal = awayGoalieSavesCombinedByPeriod.sum

  lazy val firstPeriodHomeTotalShots = firstPeriodTotalShots(home)
  lazy val firstPeriodAwayTotalShots = firstPeriodTotalShots(away)
  lazy val firstPeriodTeamWithMostShots = mtch.teams.find(hasMoreFirstPeriodShots)
  lazy val firstPeriodTeamWithMostShotsTotalShots = firstPeriodTeamWithMostShots.map(firstPeriodTotalShots)
  lazy val firstPeriodTeamWithLessShots = mtch.teams.find(hasLessFirstPeriodShots)
  lazy val firstPeriodTeamWithLessShotsTotalShots = firstPeriodTeamWithLessShots.map(firstPeriodTotalShots)
  lazy val firstPeriodHasBigShotDifference = firstPeriodTeamWithMostShotsTotalShots.exists(t => firstPeriodTeamWithLessShotsTotalShots.exists(t2 => t >= t2 * 2))

  lazy val firstPeriodWinningTeam = mtch.teams.find(isWinningInFirstPeriod)
  lazy val firstPeriodWinningTeamIsHome = firstPeriodWinningTeam.exists(t => t.id == home.id)
  lazy val firstPeriodLosingTeam = mtch.teams.find(isLosingInFirstPeriod)
  lazy val firstPeriodLeadingTeam = mtch.teams.find(isLeadingAfterFirstPeriod)
  lazy val secondPeriodLeadingTeam = mtch.teams.find(isLeadingAfterSecondPeriod)
  lazy val teamOvertakingInSecondPeriod = firstPeriodLosingTeam.find(isLeadingAfterSecondPeriod)
  lazy val teamTyingInSecondPeriod = firstPeriodLosingTeam.find(isTiedAfterSecondPeriod)
  lazy val teamOvertakingInSecondPeriodLoses = teamOvertakingInSecondPeriod.exists(_.id == loser.id)
  lazy val teamTyingInSecondPeriodLoses = teamTyingInSecondPeriod.exists(_.id == loser.id)
  lazy val tiedAfterSecondPeriod = homeSecondPeriodGoalDiff == 0
  lazy val secondPeriodWithoutGoals = secondPeriodTotalGoals == 0

  lazy val secondPeriodLeadWithOne = secondPeriodGoalDiff == 1

  lazy val secondPeriodOvertakingGoal = goals.filter(isSecondPeriodGoal).find(isOvertakingGoal)
  lazy val secondPeriodOvertakingGoalTime = secondPeriodOvertakingGoal.flatMap(_.gameTime).getOrElse("")
  lazy val secondPeriodOvertakingGoalPlayer = secondPeriodOvertakingGoal.flatMap(f => findPlayerInTeam(f.player, firstPeriodLosingTeam))

  lazy val secondPeriodTyingGoal = goals.filter(isSecondPeriodGoal).find(isTyingGoal)
  lazy val secondPeriodTyingGoalTime = secondPeriodTyingGoal.flatMap(_.gameTime).getOrElse("")
  lazy val secondPeriodTyingGoalPlayer = secondPeriodTyingGoal.flatMap(f => findPlayerInTeam(f.player, firstPeriodLosingTeam))

  lazy val secondPeriodLastTyingGoal = goals.filter(isSecondPeriodGoal).filter(isTyingGoal).lastOption
  lazy val secondPeriodLastTyingGoalTime = secondPeriodLastTyingGoal.flatMap(_.gameTime).getOrElse("")
  lazy val secondPeriodLastTyingGoalPlayer = secondPeriodLastTyingGoal.map(_.player.get)

  lazy val winnerBiggestGoalLead = biggestGoalLead(winner)
  lazy val loserBiggestGoalLead = biggestGoalLead(loser)
  lazy val winnerSmallestGoalLead = smallestGoalLead(winner)
  lazy val winnerSmallestGoalLeadInThirdPeriod = smallestGoalLeadInThirdPeriod(winner)
  lazy val wonInRegulation = winner.score.get.outcome.wins == 1
  lazy val wonInOvertime = winner.score.get.outcome.otWins == 1 && winner.score.get.periods.last.id == "4"
  lazy val wonInShootout = winner.score.get.outcome.otWins == 1 && winner.score.get.periods.last.id == "5"

  lazy val winnerSmallestGoalLeadInThirdPeriodGoal = smallestGoalLeadInThirdPeriodGoal(winner)
  lazy val winnerBiggestGoalLeadGoal = biggestGoalLeadGoal(winner)
  lazy val biggestGoalLeadBeforeSmallestGoalLead = gameTimeInSeconds(winnerBiggestGoalLeadGoal._1.gameTime.get) < gameTimeInSeconds(winnerSmallestGoalLeadInThirdPeriodGoal._1.gameTime.get)
  lazy val loserCatchedUpFromThreeGoalsToOne = winnerBiggestGoalLead >= 3 && winnerSmallestGoalLeadInThirdPeriod.contains(1) && biggestGoalLeadBeforeSmallestGoalLead
  lazy val winnerWasAtLeastTwoGoalsBehind = winnerSmallestGoalLead.exists(_ <= -2)
  lazy val winnerWasBehindAfterSecondPeriod = secondPeriodLeadingTeam.exists(_.id != winner.id)
  lazy val winnerCatchedUpAndWonInRegulation = loserBiggestGoalLead > 2 && winnerWasBehindAfterSecondPeriod && wonInRegulation
  lazy val winnerCatchedUpAndWonInOvertime = loserBiggestGoalLead > 2 && winnerWasBehindAfterSecondPeriod && wonInOvertime
  lazy val winnerCatchedUpAndWonInShootout = loserBiggestGoalLead > 2 && winnerWasBehindAfterSecondPeriod && wonInShootout
  lazy val winnerCatchedUpAndWonInAdditionalPeriod = loserBiggestGoalLead > 2 && winnerWasBehindAfterSecondPeriod && hasAdditionalPeriods

  lazy val goalsInThirdPeriod = goals.filter(isThirdPeriod)
  lazy val lastGoalInThirdPeriod = goalsInThirdPeriod.lastOption
  lazy val lastGoalInThirdPeriodPlayer = lastGoalInThirdPeriod.map(_.player.get)
  lazy val lastTyingGoalInThirdPeriodPlayer = goalsInThirdPeriod.filter(isTyingGoal).lastOption.map(_.player.get)
  lazy val loserTiedWithLastGoalInThirdPeriod = lastGoalInThirdPeriod.filter(isLosingTeam).exists(isTyingGoal)
  lazy val loserTiedInThirdPeriod = goalsInThirdPeriod.filter(isLosingTeam).exists(isTyingGoal)
  lazy val loserHasZeroGoals = loser.score.get.now == 0
  lazy val winnerHasThreeOrMoreGoals = winner.score.get.now >= 3
  lazy val isShutoutWithThreeOrMoreGoals = loserHasZeroGoals && winnerHasThreeOrMoreGoals
  lazy val winnerInLeadTheWholeGame = smallestGoalLead(winner).exists(_ > 0)

  // Playoffs
  lazy val hasAdditionalPeriods = winner.score.get.outcome.otWins == 0 && winner.score.get.periods.last.id.toInt > 3
  lazy val lastAdditionalPeriodNumber = if (hasAdditionalPeriods) Some(winner.score.get.periods.last.id.toInt - 3) else None


  private def isOvertakingGoal(feed: Feed) = overtakingGoals.exists(overtakingGoal => overtakingGoal._1 == feed)
  private def isTyingGoal(feed: Feed) = tyingGoals.exists(tyingGoal => tyingGoal._1 == feed)

  private def goalDiff(score: Score) = (score.away - score.home).abs
  private def teamGoalDiff(score: Score, team: Team) =
    if (team.id == away.id) score.away - score.home else score.home - score.away
  def otherTeam(team: Team) = mtch.teams.filterNot(t => t.id == team.id).head
  def otherTeam(team: Option[Team]) = team.map(p => mtch.teams.filterNot(t => t.id == p.id).head)

  private def isLosingInFirstPeriod(team: Team) = firstPeriodScore(team) < firstPeriodScore(otherTeam(team))
  private def isWinningInFirstPeriod(team: Team) = firstPeriodScore(team) > firstPeriodScore(otherTeam(team))
  private def isLeadingAfterFirstPeriod(team: Team) = firstPeriodScore(team) > firstPeriodScore(otherTeam(team))
  private def isLeadingAfterSecondPeriod(team: Team) = secondPeriodScore(team) > secondPeriodScore(otherTeam(team))
  private def isTiedAfterSecondPeriod(team: Team) = secondPeriodScore(team) == secondPeriodScore(otherTeam(team))

  private def isSecondPeriodGoal(feed: Feed) = isScore(feed) && isSecondPeriod(feed)

  private def isScore(feed: Feed) = feed.`type`.contains("score")
  private def isPeriod(id: String) = (feed: Feed) => feed.period.contains(id)
  private def isFirstPeriod(feed: Feed) = feed.period.contains("1")
  private def isSecondPeriod(feed: Feed) = feed.period.contains("2")
  private def isThirdPeriod(feed: Feed) = feed.period.contains("3")
  private def isOvertime(feed: Feed) = feed.period.contains("4")
  private def isShootout(feed: Feed) = feed.period.contains("5")
  private def isLosingTeam(feed: Feed) = feed.team.get.id == loser.id


  private def smallestGoalLeadInThirdPeriodGoal(team: Team) = goalsWithCurrentScore.filter(s => isThirdPeriod(s._1)).filter(p => teamGoalDiff(p._2, team) == smallestGoalLeadInThirdPeriod(team).get).head
  private def biggestGoalLeadGoal(team: Team) = goalsWithCurrentScore.filter(p => teamGoalDiff(p._2, team) == biggestGoalLead(team)).head
  private def biggestGoalLead(team: Team) = goalsWithCurrentScore.map(s => teamGoalDiff(s._2, team)).max
  private def smallestGoalLeadInThirdPeriod(team: Team) = minOption(goalsWithCurrentScore.filter(s => isThirdPeriod(s._1)).map(s => teamGoalDiff(s._2, team)))
  private def smallestGoalLead(team: Team) = minOption(goalsWithCurrentScore.map(s => teamGoalDiff(s._2, team)))

  def min(a: Int, b: Int) = if (a < b) a else b
  def minOption(seq: Seq[Int]) = seq.reduceOption(min)


  lazy val goals = mtch.feed.filter(isScore)
  lazy val goalsWithCurrentScore = goals.map(f => withCurrentScore(f, scoresBefore(f)))
  lazy val overtakingGoals = goalsWithCurrentScore.filter(w => goalDiff(w._2) == 1)
  lazy val tyingGoals = goalsWithCurrentScore.filter(w => goalDiff(w._2) == 0)

  def scoresBefore(feed: Feed) = goals.takeWhile(_ != feed)

  case class Score(home: Int, away: Int)
  def withCurrentScore(score: Feed, scoresBefore: List[Feed]) = {
    val teamScores = (scoresBefore :+ score).groupBy(_.team.get.id)
    val homeScore = teamScores.find(_._1 == home.id).map(_._2.length).getOrElse(0)
    val awayScore = teamScores.find(_._1 == away.id).map(_._2.length).getOrElse(0)
    (score, Score(homeScore, awayScore))
  }

  def gameTimeInSeconds(gameTime: String): Int = {
    val minsAndSecs = gameTime.split(":")
    val minutes = minsAndSecs.head
    val secs = minsAndSecs(1)
    minutes.toInt * 60 + secs.toInt
  }

  def scoreAtTime(time: String): Score = {
    val homeScore = goals.filter(g => (g.team.get.id == home.id && isBeforeOrEqual(g.gameTime.get, time))).length
    val awayScore = goals.filter(g => (g.team.get.id == away.id && isBeforeOrEqual(g.gameTime.get, time))).length

    Score(homeScore, awayScore)
  }

  private def isBeforeOrEqual(t1: String, t2: String): Boolean = (gameTimeInSeconds(t1) <= gameTimeInSeconds(t2))

  private def findPlayerInTeam(feedPlayer: FeedPlayer, team: Team): Option[Player] = team.players.find(_.id == feedPlayer.id)
  private def findPlayerInTeam(feedPlayerOpt: Option[FeedPlayer], teamOpt: Option[Team]): Option[Player] = for {
    player <- feedPlayerOpt
    team <- teamOpt
    res <- findPlayerInTeam(player, team)
  } yield res

  private lazy val assists = mtch.feed.filter(_.`type` == "assist")
  private def assistsForScore(score: Feed) = assists.filter(_.gameTime == score.gameTime)
  private def goalieForPeriod(team: Team, period: String) = goalkeepings.filter(isTeam(team)).find(isPeriod(period)).flatMap(_.player)

  private lazy val goalkeepings = mtch.feed.filter(_.`type` == "goalkeeping")
  private def isTeam(team: Team) = (feed: Feed) => feed.team.exists(_.id == team.id)

  private def goalieSavesByPeriod(team: Team, period: Int): Option[Int] = {
    if (goalkeepings.exists(isPeriod(period.toString))) {
      Some(goalkeepings.filter(isPeriod(period.toString)).filter(isTeam(team)).map(_.saves.get.toInt).sum)
    } else None
  }

  private def combineGoalieSaves(team: Team): List[Int] = {
    List() ++ goalieSavesByPeriod(team, 1) ++ goalieSavesByPeriod(team, 2) ++ goalieSavesByPeriod(team, 3) ++ goalieSavesByPeriod(team, 4) ++ goalieSavesByPeriod(team, 5)
  }

  private def firstPeriodTotalShots(team: Team) = team.players.map(Stats.getPerPeriod).flatMap(_.get("1")).map(s => s.missedShots + s.shotsOnGoal).sum
  private def hasMoreFirstPeriodShots(team: Team) = firstPeriodTotalShots(team) > firstPeriodTotalShots(otherTeam(team))
  private def hasLessFirstPeriodShots(team: Team) = firstPeriodTotalShots(team) < firstPeriodTotalShots(otherTeam(team))
}
