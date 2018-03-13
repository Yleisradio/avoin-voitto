package liigavoitto.journalist

import liigavoitto.journalist.values.MatchDataValues
import liigavoitto.scores.{Match, Period, ScoresApiParser, Team}

object TestUtils extends TestUtils

trait TestUtils extends ScoresApiParser {

  val testLanguages = List("fi", "sv")

  def matchFromJson(resourceName: String) = {
    matchesFromJson(resourceName).head
  }

  def matchesFromJson(resourceName: String) = {
    val json = scala.io.Source.fromFile(s"src/test/resources/$resourceName", "utf8").mkString
    parseMatchList(json).get
  }

  def leagueTableFromJson(resourceName: String) = {
    val json = scala.io.Source.fromFile(s"src/test/resources/$resourceName", "utf8").mkString
    parseLeagueTable(json).get
  }

  def playerStatsFromJson(resourceName: String) = {
    val json = scala.io.Source.fromFile(s"src/test/resources/$resourceName", "utf8").mkString
    parsePlayerStats(json).get
  }

  def leagueFromJson(resourceName: String) = {
    val json = scala.io.Source.fromFile(s"src/test/resources/$resourceName", "utf8").mkString
    parseMatchSport(json).get.series.head.id
  }

  def replaceStats(mtch: Match, playerId: String, totalStats: Map[String, Any]) = {
    val statsAsBigInt = totalStats.map(entry => entry._1 -> BigInt(entry._2.asInstanceOf[Int]))
    val team = mtch.teams.find(_.players.exists(p => p.id == playerId)).get
    val player = team.players.find(_.id == playerId).get
    val replacedPlayer = player.copy(specific = player.specific.updated("stats", Map("total" -> statsAsBigInt)))
    val replacedTeam = team.copy(players = replacedPlayer :: team.players diff List(player))
    mtch.copy(teams = mtch.teams.filterNot(_.id == replacedTeam.id) :+ replacedTeam)
  }

  def withResult(mtch: Match, homeScore: Int, awayScore: Int) = {
    def replaceScore(team: Team, score: Int) = {
      team.copy(score = Some(team.score.get.copy(now = score)))
    }
    mtch.copy(teams = List(
      replaceScore(mtch.teams.head, homeScore),
      replaceScore(mtch.teams(1), awayScore)
    ))
  }

  def withExtraPeriods(mtch: Match, addPeriods: Int) = {
    mtch.copy(teams = mtch.teams.map(t => t.copy(score =
      Some(t.score.get.copy(periods =
        t.score.get.periods ++ (1 to addPeriods).map(_ => Period("", 0)))))))
  }
}

trait MockData extends TestUtils {
  val leagueTable = leagueTableFromJson("scores/mestis/league-table.json")
  val mtch = matchFromJson("scores/mestis/jkl-0-2018-3776.json")
  val league = leagueFromJson("scores/mestis/jkl-0-2018-3776.json")
  val md = MatchData(mtch, league, "2017-2018", "171", allHomeTeamMatches = List(), allAwayTeamMatches = List(), leagueTable = leagueTable)
  val values = MatchDataValues(md, "fi")
}
