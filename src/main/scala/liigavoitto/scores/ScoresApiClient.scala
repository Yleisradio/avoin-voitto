package liigavoitto.scores

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import liigavoitto.util.Logging
import org.joda.time.DateTime

import scala.concurrent.{ Await, Future }

class ScoresApiClient(implicit val system: ActorSystem) extends ScoresApiSupport with ScoresApiParser with Logging {

  def getMatchAsSport(id: String): Future[Sport] = {
    val uri = Uri(s"${apiUrl}match/$id.json") withQuery Query(scoresAuth)
    log.info(s"URL: $uri")
    Future[Sport] {
      parseMatchSport(Await.result(get(uri.toString), timeout)) match {
        case Some(m) => m
        case None => throw new Exception(s"Error fetching match by id: $id")
      }
    }
  }

  def getMatch(id: String) = getMatchAsSport(id).map(s => extractMatchesFromSport(s).head)

  def getMatches(sport: String, series: String, season: String, stage: String,
    from: Option[DateTime] = None,
    to: Option[DateTime] = None,
    teamId: Option[String] = None,
    include: String = "all",
    exclude: String = "") = {
    val uri = matchesQuery(sport, series, season, stage, from, to, teamId, include, exclude)
    log.info(s"URL: $uri")
    Future[List[Match]] {
      parseMatchList(Await.result(get(uri.toString), timeout)) match {
        case Some(l) => l
        case None => List()
      }
    }
  }

  def matchesQuery(sport: String, series: String, season: String, stage: String,
    from: Option[DateTime] = None,
    to: Option[DateTime] = None,
    teamId: Option[String] = None,
    include: String = "all",
    exclude: String = "") = {
    var query = scoresAuth
    query += ("sportTags" -> sport, "seriesTags" -> series, "seasonTags" -> season, "stageTags" -> stage)
    query += includeParam(include)
    query += excludeParam(exclude)
    if (from.isDefined) query ++= Map("dateFrom" -> dateFormat.print(from.get))
    if (to.isDefined) query ++= Map("dateTo" -> dateFormat.print(to.get))
    if (teamId.isDefined) query += ("teamId" -> teamId.get)
    Uri(s"${apiUrl}matches.json") withQuery Query(query)
  }

  def getLeagueTable(sport: String, series: String, season: String, stage: String) = {
    val uri = leagueTableUri(sport, series, season, stage)
    log.info(s"URL: $uri")
    Future[List[LeagueTableEntry]] {
      parseLeagueTable(Await.result(get(uri.toString), timeout)) match {
        case Some(l) => l
        case None => List()
      }
    }
  }

  def getGeneratedPlayerStats(sport: String, series: String, season: String, stage: String, toDate: Option[DateTime] = None) = {
    val uri = generatedPlayerStatsUri(sport, series, season, stage, toDate)
    log.info(s"URL: $uri")
    Future[List[PlayerStatsEntry]] {
      parsePlayerStats(Await.result(get(uri.toString), timeout)) match {
        case Some(ps) => ps
        case None => List()
      }
    }
  }
  
  def getPlayerStats(sport: String, series: String, season: String, stage: String) = {
    val uri = playerStatsUri(sport, series, season, stage)
    log.info(s"URL: $uri")
    Future[List[PlayerStatsEntry]] {
      parsePlayerStats(Await.result(get(uri.toString), timeout)) match {
        case Some(ps) => ps
        case None => List()
      }
    }
  }

  def leagueTableUri(sport: String, series: String, season: String, stage: String) = {
    val stageId = "stageId" -> stage
    Uri(s"${apiUrl}sports/$sport/series/$series/seasons/$season/league-table.json") withQuery Query(scoresAuth + stageId + includeParam("all"))
  }
  
  def playerStatsUri(sport: String, series: String, season: String, stage: String) = {
    val stageId = "stageId" -> stage
    Uri(s"${apiUrl}sports/$sport/series/$series/seasons/$season/player-stats.json") withQuery Query(scoresAuth + stageId + includeParam("all"))
  }

  def generatedPlayerStatsUri(sport: String, series: String, season: String, stage: String, dateTo: Option[DateTime]) = {
    var query = scoresAuth
    query += "stageId" -> stage
    if (dateTo.isDefined) query += "dateTo" -> dateFormat.print(dateTo.get)
    Uri(s"${apiUrl}sports/$sport/series/$series/seasons/$season/generated-player-stats.json") withQuery Query(query + includeParam("all"))
  }

  def includeParam(include: String) = "include" -> include

  def excludeParam(exclude: String) = "exclude" -> exclude
}
