package liigavoitto.fetch

import liigavoitto.journalist.MatchData
import liigavoitto.journalist.values.SeriesValues
import liigavoitto.scores.{ScoresApiClient, Series}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class ScoresFetcher(matchId: String, api: ScoresApiClient) {

  val timeout = 30.seconds

  def getEnrichedMatchData = {
    Await.result[MatchData]({
      api.getMatchAsSport(matchId).flatMap(
        sport => {
          val series = sport.series.head
          val season = series.seasons.head
          val stage = season.stages.head
          val regularSeasonStage = new SeriesValues(series.id).regularSeasonStageId.getOrElse(stage.id)
          val mtch = stage.matches.get.head
          val dateTo = mtch.date
          for (
            allHomeTeamMatches <- api.getMatches(sport.id, series.id, season.id, stage.id, None, Some(dateTo), Some(mtch.teams(0).id));
            allAwayTeamMatches <- api.getMatches(sport.id, series.id, season.id, stage.id, None, Some(dateTo), Some(mtch.teams(1).id));
            leagueTable <- api.getLeagueTable(sport.id, series.id, season.id, regularSeasonStage);
            playerStats <- api.getPlayerStats(sport.id, series.id, season.id, stage.id)
          ) yield {
            MatchData(mtch, series.id, season.id, stage.id, allHomeTeamMatches, allAwayTeamMatches, leagueTable, playerStats)
          }
        }
      )
    }, timeout)
  }
}
