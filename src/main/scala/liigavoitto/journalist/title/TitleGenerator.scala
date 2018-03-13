package liigavoitto.journalist.title

import liigavoitto.journalist.MatchData
import liigavoitto.journalist.values.MatchDataValues

class TitleGenerator(matchData: MatchData, language: String)
  extends PrimaryTitleText
    with PlayoffsTitleText
    with ImportantPlayerTitleExtension {

  val lang = language
  val values = MatchDataValues(matchData, lang)

  def generateTitle: Option[String] = {
    val titles = if (values.isPlayoffs) playoffsTitleTexts else primaryTitleTexts
    weightedRandom(titles).map(primaryTitle => {
      val secondaryTitles = getImportantPlayerExtensions
      weightedRandom(secondaryTitles)
        .map(secondaryTitle => primaryTitle + " – " + secondaryTitle)
        .getOrElse(primaryTitle)
    })
  }
}
