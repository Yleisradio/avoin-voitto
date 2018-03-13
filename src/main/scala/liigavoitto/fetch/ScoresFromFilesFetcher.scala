package liigavoitto.fetch

import liigavoitto.journalist.MatchData
import liigavoitto.scores.ScoresApiParser
import org.joda.time.DateTime

class ScoresFromFilesFetcher(matchId: String) extends ScoresApiParser {

  def getEnrichedMatchData: MatchData = {
    {
      val sport = matchFromJson(matchId)
      val series = sport.series.head
      val season = series.seasons.head
      val stage = season.stages.head
      val mtch = stage.matches.get.head
      for(
        allHomeTeamMatches <- matchesFromJson(mtch.teams.head.id, series.id, mtch.date);
        allAwayTeamMatches <- matchesFromJson(mtch.teams(1).id, series.id, mtch.date);
        leagueTable <- leagueTableFromJson(series.id);
        playerStats <- playerStatsFromJson(series.id)
      ) yield MatchData(mtch, series.id, season.id, stage.id, allHomeTeamMatches, allAwayTeamMatches, leagueTable, playerStats)
    }.get
  }


  def matchFromJson(matchId: String) = {
    val json = scala.io.Source.fromFile(s"data/matches/$matchId.json", "utf8").mkString
    parseMatchSport(json).get
  }

  def matchesFromJson(teamId: String, seriesId: String, toDate: DateTime) = {
    val json = scala.io.Source.fromFile(s"data/$seriesId/team-histories/$teamId.json", "utf8").mkString
    parseMatchList(json).map(p => p.filter(m => toDate.getMillis >= m.date.getMillis))
  }

  def leagueTableFromJson(seriesId: String) = {
    val json = scala.io.Source.fromFile(s"data/$seriesId/league.json", "utf8").mkString
    parseLeagueTable(json)
  }

  def playerStatsFromJson(seriesId: String) = {
    val json = scala.io.Source.fromFile(s"data/$seriesId/player.json", "utf8").mkString
    parsePlayerStats(json)
  }

}
