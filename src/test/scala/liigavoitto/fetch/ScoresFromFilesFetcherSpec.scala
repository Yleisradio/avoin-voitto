package liigavoitto.fetch

import org.scalatest.{FlatSpec, Matchers}

class ScoresFromFilesFetcherSpec extends FlatSpec with Matchers {

  "ScoresFromFilesFetcher" should "fetch match data from files" in {
    val matchId = "jkl-0-2018-3814"
    val fetcher = new ScoresFromFilesFetcher(matchId)
    val data = fetcher.getEnrichedMatchData

    data.mtch.id shouldEqual matchId
    data.seriesId shouldEqual "mestis"
    data.allAwayTeamMatches.length shouldEqual 38
    data.allHomeTeamMatches.length shouldEqual 36
    data.playerStats.length shouldEqual 466
    data.leagueTable.length shouldEqual 12
  }

  it should "filter matches in match list in older games" in {
    val matchId = "jkl-0-2018-3748"
    val fetcher = new ScoresFromFilesFetcher(matchId)
    val data = fetcher.getEnrichedMatchData

    data.mtch.id shouldEqual matchId
    data.seriesId shouldEqual "mestis"
    data.allAwayTeamMatches.length shouldEqual 17
    data.allHomeTeamMatches.length shouldEqual 18
  }
}
