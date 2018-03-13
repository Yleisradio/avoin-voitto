package liigavoitto.journalist.values

import liigavoitto.journalist.MatchData
import liigavoitto.scores.Team

trait LeagueTableValues {
  object TeamPlacementGroup extends Enumeration {
    val First, Top, Mid, Low = Value
  }

  implicit val data: MatchData

  implicit val home: Team
  implicit val away: Team

  lazy val leagueTable = data.leagueTable
  lazy val bestTeam = leagueTable.head.team
  lazy val secondBestTeam = leagueTable(1).team

  lazy val worstTeam = leagueTable.last.team
  lazy val secondWorstTeam = leagueTable(leagueTable.length - 2).team

  private lazy val teamIds = data.mtch.teams.map(_.id)
  lazy val isMatchBetweenTheBest = teamIds.contains(bestTeam.id) && teamIds.contains(secondBestTeam.id)
  lazy val isMatchBetweenTheWorst = teamIds.contains(worstTeam.id) && teamIds.contains(secondWorstTeam.id)

  lazy val homeTeamPlacement = leagueTable.indexWhere(e => e.team.id == home.id) + 1
  lazy val awayTeamPlacement = leagueTable.indexWhere(e => e.team.id == away.id) + 1

  lazy val homeTeamPlacementGroup = findPlacementGroup(homeTeamPlacement)
  lazy val awayTeamPlacementGroup = findPlacementGroup(awayTeamPlacement)

  lazy val teamsInSamePlacementGroup = homeTeamPlacementGroup == awayTeamPlacementGroup

  private def findPlacementGroup(placement: Int) = {
    val firstPlacePlacement = 1
    val topPlacement = 3
    val bottomPlacement = 10

    placement match {
      case p if (p == firstPlacePlacement) => TeamPlacementGroup.First
      case p if (p <= topPlacement) => TeamPlacementGroup.Top
      case p if (p <= bottomPlacement) => TeamPlacementGroup.Mid
      case _ => TeamPlacementGroup.Low
    }
  }
}
