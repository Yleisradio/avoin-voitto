package liigavoitto.journalist.lead

import liigavoitto.journalist.text.CommonImplicits
import liigavoitto.journalist.utils.{Template}

trait BestOrWorstTeamsText extends CommonImplicits {

  private val filePath = "template/lead/best-or-worst-teams-text.edn"
  private def tmpl = getTemplateFn(filePath)

  def bestOrWorstTeamsTemplates: List[Template] =
    if (values.isMatchBetweenTheBest) tmpl("match-between-the-best")
    else if (values.isMatchBetweenTheWorst) tmpl("match-between-the-worst")
    else List()

  def bestOrWorstTeamsTexts = bestOrWorstTeamsTemplates.flatMap(t => render(t, templateAttributes))

  def bestOrWorstTeamsText = weightedRandom(bestOrWorstTeamsTexts)

  private def templateAttributes = Map(
    "day:capitalize" -> values.day.capitalize,
    "day" -> values.day
  ) ++
    teamWithDeclensions("home", values.home, lang) ++
    teamWithDeclensions("away", values.away, lang)
}
