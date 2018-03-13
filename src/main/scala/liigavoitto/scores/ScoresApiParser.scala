package liigavoitto.scores

import org.joda.time.DateTime
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods._
import liigavoitto.util.{ DateTimeNoMillisSerializer, Logging }

import scala.util.{ Failure, Success, Try }

case class Data(data: List[Sport])
case class Sport(id: String, series: List[Series])
case class Series(id: String, seasons: List[Season])
case class Season(id: String, stages: List[Stage])
case class Stage(id: String, matches: Option[List[Match]],
  standing: Option[List[LeagueTableEntry]],
  playerStatistics: Option[List[PlayerStatsEntry]])

case class Match(id: String, name: String, date: DateTime, status: String, teams: List[Team], feed: List[Feed] = List(), stats: GeneralMatchStats)
case class Feed(`type`: String, gameTime: Option[String], period: Option[String], player: Option[FeedPlayer], standing: Option[String], team: Option[Team], goalType: Option[String], saves: Option[String], timeInMins: Option[String], text: Option[String], beginTime: Option[String], endTime: Option[String])
case class FeedPlayer(id: String, name: PlayerName, meta: Option[PlayerMeta])
case class Team(id: String, name: String, abbr: String, meta: Meta, score: Option[Score], players: List[Player])
case class Score(now: Int, periods: List[Period], outcome: Outcome)
case class Period(id: String, score: Int)
case class Meta(images: List[Image], directives: Option[Map[String, Any]])
case class Image(id: String, imageType: String)
case class Outcome(wins: Int, draws: Int, losses: Int, otWins: Int, otLosses: Int)

case class Player(id: String, name: PlayerName, position: Option[String], specific: Map[String, Any], meta: Option[PlayerMeta])
case class PlayerName(first: String, last: String)
case class PlayerMeta(gender: Option[String] = None, country: Option[String] = None, tags: List[String] = Nil, directives: Map[String, String] = Map.empty)

case class LeagueTableEntry(
  team: Team,
  home: Option[LeagueTableResult] = None,
  away: Option[LeagueTableResult] = None,
  total: Option[LeagueTableResult] = None,
  specific: Option[Map[String, Any]] = None
)
case class LeagueTableResult(gamesPlayed: Int, outcome: Outcome, goals: Goals, points: Option[Int] = None, specific: Option[Map[String, Any]] = None)
case class Goals(score: Int, conceded: Int)

case class PlayerStatsEntry(player: PlayerStatsPlayer, teamStats: List[PlayerTeamStatsEntry])
case class PlayerTeamStatsEntry(team: Team, points: Int, goals: Int, assists: Int)
case class PlayerStatsPlayer(id: String, name: PlayerName)

case class GeneralMatchStats(attendance: Int)

trait ScoresApiParser extends Logging {
  implicit val formats = DefaultFormats + DateTimeNoMillisSerializer

  def parseMatchSport(json: String): Option[Sport] = extractData(json).map(_.head)
  def parseMatchList(json: String): Option[List[Match]] = extractData(json) match {
    case Some(sports) => if (sports.nonEmpty) Some(extractMatchesFromSport(sports.head).get) else Some(List())
    case None => None
  }
  def parseLeagueTable(json: String): Option[List[LeagueTableEntry]] = {
    extractData(json) match {
      case Some(sports) => if (sports.nonEmpty) Some(extractLeagueTableFromSport(sports.head).get) else Some(List())
      case None => None
    }
  }
  def parsePlayerStats(json: String): Option[List[PlayerStatsEntry]] = {
    extractData(json) match {
      case Some(sports) =>
        if (sports.nonEmpty)
          Some(extractPlayerStatsFromSport(sports.head).get)
        else Some(List())
      case None => None
    }
  }

  protected def extractMatchesFromSport(sport: Sport) = sport.series.head.seasons.head.stages.head.matches
  protected def extractLeagueTableFromSport(sport: Sport) = sport.series.head.seasons.head.stages.head.standing
  protected def extractPlayerStatsFromSport(sport: Sport) = sport.series.head.seasons.head.stages.head.playerStatistics
  protected def extractData(json: String) = {
    Try {
      log.debug(s"Sport JSON: $json")
      parse(json).extract[Data]
    } match {
      case Success(s) => Some(s.data)
      case Failure(e) =>
        log.info(s"Failed to parse '$json': " + e)
        None
    }
  }
}
