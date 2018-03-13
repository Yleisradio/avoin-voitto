package liigavoitto.journalist

import liigavoitto.journalist.author.AuthorGenerator
import liigavoitto.journalist.concepts.ConceptIdGenerator
import liigavoitto.journalist.events.GameEventsGenerator
import liigavoitto.journalist.stats.GameStatsTableGenerator
import liigavoitto.journalist.text.TextGenerator
import liigavoitto.journalist.title.TitleGenerator
import liigavoitto.scores.{LeagueTableEntry, Match, PlayerStatsEntry}
import liigavoitto.transform._
import liigavoitto.util.Logging
import org.joda.time.DateTime

case class MatchData(
                      mtch: Match,
                      seriesId: String,
                      season: String,
                      stage: String,
                      allHomeTeamMatches: List[Match] = List(),
                      allAwayTeamMatches: List[Match] = List(),
                      leagueTable: List[LeagueTableEntry] = List(),
                      playerStats: List[PlayerStatsEntry] = List()
                    )

object LiigaJournalist extends Logging with ArticlesV2Transformer {

  def createArticle(data: MatchData, language: String): Option[ArticleV2] = {
    val mtch = data.mtch
    if (mtch.status == "finished") {
      val id = mtch.id

      val title = new TitleGenerator(data, language).generateTitle.get
      val gen = new TextGenerator(data, language)
      val events = new GameEventsGenerator(data, language).getEventContentBlocks
      val gameStats = new GameStatsTableGenerator(data, language).getTable
      val concepts = new ConceptIdGenerator(data.seriesId, data.mtch).getIds
      val properties = List("importance:low")
      val author = Some(AuthorGenerator.getAuthor)

      Some(
        getV2Article(
          Article(
            id,
            title,
            gen.lead,
            gen.body,
            DateTime.now,
            List(),
            List(),
            events,
            gameStats,
            concepts,
            author,
            None,
            None,
            language,
            "Yle Urheilu",
            "national",
            None,
            properties,
            gen.shortSummary)
          )
        )
    } else None
  }
}
