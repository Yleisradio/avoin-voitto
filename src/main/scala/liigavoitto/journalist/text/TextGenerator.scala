package liigavoitto.journalist.text

import liigavoitto.journalist.MatchData
import liigavoitto.journalist.body.{GameProgressTexts, HeadToHeadText, PlacementText, WinPercentageText}
import liigavoitto.journalist.utils.{TemplateLoader, TemplateUtils, WeightedRandomizer}
import liigavoitto.journalist.lead.{BestOrWorstTeamsText, KeyPlayerText, PlayoffSeriesSituationText, TeamStreakText}
import liigavoitto.journalist.values.MatchDataValues

trait CommonImplicits extends TemplateUtils with WeightedRandomizer with TemplateLoader {
  val values: MatchDataValues
  val lang: String
  def getTemplateFn(filePath: String) = (name: String) => load(filePath, name, lang)
}

class TextGenerator(matchData: MatchData, language: String)
  extends BestOrWorstTeamsText
    with TeamStreakText
    with KeyPlayerText
    with GameProgressTexts
    with WinPercentageText
    with PlacementText
    with HeadToHeadText
    with PlayoffSeriesSituationText {

  val lang: String = language
  val values: MatchDataValues = MatchDataValues(matchData, lang)

  def lead: String = if (leadTexts.nonEmpty) leadTexts.mkString(" ") else ""
  def body: List[String] = bodyTexts.grouped(2).map(g => g.mkString(" ")).toList
  def shortSummary: String = values.home.name + " " + values.home.score.get.now + " - " + values.away.name + " " + values.away.score.get.now

  private def leadTexts: List[String] = List(
    addListCond(values.isRegularSeason, bestOrWorstTeamsText),
    addListCond(values.isRegularSeason, teamStreakText),
    addListCond(values.isPlayoffs, playoffSeriesSituationText),
    keyPlayerText
  ).flatten

  private def bodyTexts: List[String] = List(
    gameStartText,
    midGameText,
    lateGameText,
    addListCond(values.isRegularSeason, winPercentageText),
    addListCond(values.isRegularSeason, placementText),
    addListCond(values.isRegularSeason, headToHeadText)
  ).flatten

  def addListCond(cond: Boolean, str: => Option[String]) = if (cond) str else None
}
