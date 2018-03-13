package liigavoitto.api

import akka.actor.ActorSystem
import liigavoitto.fetch.{ScoresFetcher, ScoresFromFilesFetcher}
import liigavoitto.journalist.LiigaJournalist
import liigavoitto.scores.ScoresApiClient
import liigavoitto.util.DateTimeNoMillisSerializer
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write

class ApiHandler(implicit val system: ActorSystem) {
  implicit val formats = Serialization.formats(NoTypeHints) + DateTimeNoMillisSerializer

  val api = new ScoresApiClient()

  def report(matchId: String, lang: String) = {
    val fetcher = new ScoresFetcher(matchId, api)
    val article = LiigaJournalist.createArticle(fetcher.getEnrichedMatchData, lang)
    write(article)
  }

  def localReport(matchId: String, lang: String) = {
    val fetcher = new ScoresFromFilesFetcher(matchId)
    val article = LiigaJournalist.createArticle(fetcher.getEnrichedMatchData, lang)
    write(article)
  }
}
