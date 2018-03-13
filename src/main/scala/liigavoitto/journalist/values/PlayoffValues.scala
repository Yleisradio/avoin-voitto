package liigavoitto.journalist.values

import liigavoitto.journalist.MatchData
import liigavoitto.scores.{Match, Team}

trait PlayoffValues {
  implicit val data: MatchData
  implicit val lang: String

  implicit val home: Team
  implicit val away: Team
  implicit val winner: Team
  implicit val loser: Team
  implicit val allHomeTeamMatches: List[Match]
  implicit val allAwayTeamMatches: List[Match]
  implicit val homeTeamWin: Boolean
  implicit val homeTeamPlacement: Int
  implicit val awayTeamPlacement: Int
  implicit val seriesValues: SeriesValues

  implicit def wins(matches: List[Match], team: Team): Int
  implicit def losses(matches: List[Match], team: Team): Int

  lazy val homeCurrentSeriesMatches = allHomeTeamMatches.filter(m => (m.teams.exists(_.id == home.id) && m.teams.exists(_.id == away.id)))
  lazy val awayCurrentSeriesMatches = allAwayTeamMatches.filter(m => (m.teams.exists(_.id == home.id) && m.teams.exists(_.id == away.id)))
  lazy val seriesMatchCount = homeCurrentSeriesMatches.length
  lazy val isSeriesOpener = (seriesMatchCount == 1)

  lazy val homeCurrentSeriesWins = wins(homeCurrentSeriesMatches, home)
  lazy val awayCurrentSeriesWins = wins(awayCurrentSeriesMatches, away)
  lazy val winnerCurrentSeriesWins = if (homeTeamWin) homeCurrentSeriesWins else awayCurrentSeriesWins
  lazy val loserCurrentSeriesWins = if (homeTeamWin) awayCurrentSeriesWins else homeCurrentSeriesWins
  lazy val winnerLeads = (winnerCurrentSeriesWins > loserCurrentSeriesWins)
  lazy val winnerEqualised = winnerCurrentSeriesWins == loserCurrentSeriesWins
  lazy val winnerTookTheLead = winnerCurrentSeriesWins == loserCurrentSeriesWins + 1
  lazy val winnerOneWinAwayFromSeriesWin = winnerCurrentSeriesWins == playoffRounds(roundNumber - 1).winsRequired - 1

  lazy val playoffRounds = seriesValues.playoffRounds

  lazy val isThirdPlacePlayoff = lostPreviousMatchPair(home) && lostPreviousMatchPair(away)

  lazy val hasLimitedFirstRound = playoffRounds.map(_.teamStandings.length).distinct.length > 1

  lazy val roundNumber: Int = {
    /*
      Here is the logic that hopefully explains how we get the round number.
      It currently supports only one limited team amount first round ("säälipleijarit").
        1. round
          If no previous rounds for both teams and both of the teams are in 7-10

        Quarterfinal
          If one of the teams is 7-10 and has 1 round played
          If both teams are 1-6 and 0 previous rounds played

        Semifinal
          If one of the teams is 7-10 and has 2 rounds played
          If both teams are 1-6 and have 1 round played

        Final
          If one of the teams is 7-10 and has 3 rounds played
          If both teams are 1-6 and have 2 rounds played
    */

    val homeOpponentsCount = allHomeTeamMatches.map(m => m.teams.filter(_.id != home.id).head.id).distinct.length
    val awayOpponentsCount = allAwayTeamMatches.map(m => m.teams.filter(_.id != away.id).head.id).distinct.length
    val maxOpponentsCount = Math.max(homeOpponentsCount, awayOpponentsCount)

    if (hasLimitedFirstRound) {
      if (isLimitedFirstRoundTeam(home) && isLimitedFirstRoundTeam(away)) 1
      else if (isLimitedFirstRoundTeam(home) || isLimitedFirstRoundTeam(away)) maxOpponentsCount
      else maxOpponentsCount + 1
    }
    else maxOpponentsCount
  }

  lazy val seriesLength = {
    if (isThirdPlacePlayoff) 1 // TODO: Currently we assume there is only one match. Add the third place round to playoffRounds.
    else playoffRounds(roundNumber - 1).winsRequired * 2 - 1
  }
  lazy val isFinalSeries = roundNumber == playoffRounds.length && !isThirdPlacePlayoff
  lazy val isFinalMatch =
    homeCurrentSeriesWins == playoffRounds(roundNumber - 1).winsRequired ||
    awayCurrentSeriesWins == playoffRounds(roundNumber - 1).winsRequired

  private def isLimitedFirstRoundTeam(team: Team) = {
    val placement = if (team.id == home.id) homeTeamPlacement else awayTeamPlacement
    playoffRounds.head.teamStandings.contains(placement)
  }

  private def lostPreviousMatchPair(team: Team): Boolean = {
    val previousSeriesMatches =
      if (team.id == home.id) allHomeTeamMatches.filterNot(p => homeCurrentSeriesMatches.contains(p))
      else allAwayTeamMatches.filterNot(p => awayCurrentSeriesMatches.contains(p))

    previousSeriesMatches.nonEmpty && {
      val previousOpponent = previousSeriesMatches.last.teams.filter(_.id != team.id).head
      val previousSeries = previousSeriesMatches.filter(p => p.teams.exists(_.id == team.id) && p.teams.exists(_.id == previousOpponent.id))

      wins(previousSeries, team) < losses(previousSeries, team)
    }
  }
}
