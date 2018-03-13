package liigavoitto.journalist.utils

import liigavoitto.journalist.MockData
import liigavoitto.scores._
import org.scalatest.{FlatSpec, Matchers}

class TemplateUtilsSpec extends FlatSpec with Matchers with TemplateUtils with MockData {

  "TemplateUtilsSpec" should "return correct numeral declensions" in {
    val numberFi = numeralWithDeclensions("number6", 6, "fi")
    val numberSv = numeralWithDeclensions("number6", 6, "sv")

    numberFi("number6:nominative") shouldBe "kuusi"
    numberFi("number6:ordinalGenitive") shouldBe "kuudennen"

    numberSv("number6:nominative") shouldBe "sex"
  }

  it should "return correct declension for name" in {
    val feedPlayerDeclensions = feedPlayerWithDeclensions("testPlayer", FeedPlayer("jkl-29837610", PlayerName("Teemu", "Väyrynen"), None))
    feedPlayerDeclensions("testPlayer") shouldBe "Teemu Väyrynen"
    feedPlayerDeclensions("testPlayer:genitive") shouldBe "Teemu Väyrysen"
    feedPlayerDeclensions("testPlayer:ablative") shouldBe "Teemu Väyryseltä"
    feedPlayerDeclensions("testPlayer:allative") shouldBe "Teemu Väyryselle"
    feedPlayerDeclensions("testPlayer:adessive") shouldBe "Teemu Väyrysellä"
    feedPlayerDeclensions("testPlayer.last") shouldBe "Väyrynen"
    feedPlayerDeclensions("testPlayer.last:genitive") shouldBe "Väyrysen"
    feedPlayerDeclensions("testPlayer.last:ablative") shouldBe "Väyryseltä"
    feedPlayerDeclensions("testPlayer.last:allative") shouldBe "Väyryselle"
    feedPlayerDeclensions("testPlayer.last:adessive") shouldBe "Väyrysellä"
  }

  it should "return correct declension for team" in {
    val teamMeta = Meta(List[Image](), None)
    val teamDeclension = teamWithDeclensions("testTeam", Team("jkl-624554857", "Lukko", "Lukko", teamMeta, None, List[Player]()), "fi")
    teamDeclension("testTeam:genitive") shouldBe "Lukon"
    teamDeclension("testTeam:accusative") shouldBe "Lukon"
    teamDeclension("testTeam:allative") shouldBe "Lukolle"
    teamDeclension("testTeam:elative") shouldBe "Lukosta"
    teamDeclension("testTeam:partitive") shouldBe "Lukkoa"
  }
}
