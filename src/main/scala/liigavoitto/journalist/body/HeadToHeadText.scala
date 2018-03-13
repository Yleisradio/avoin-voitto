package liigavoitto.journalist.body

import liigavoitto.journalist.text.CommonImplicits
import liigavoitto.journalist.utils.Template

trait HeadToHeadText extends CommonImplicits {

  private val filePath = "template/body/head-to-head.edn"
  private def tmpl = getTemplateFn(filePath)

  def headToHeadTemplates: List[Template] = {
    if (values.headToHeadMatchesTotal >= 2) {
      if (values.homeTeamHeadToHeadWins > values.awayTeamHeadToHeadWins) tmpl("more-wins-for-home")
      else if (values.homeTeamHeadToHeadWins < values.awayTeamHeadToHeadWins) tmpl("more-wins-for-away")
      else tmpl("even")
    } else List()
  }

  def headToHeadTexts = headToHeadTemplates.flatMap(t => render(t, templateAttributes))
  def headToHeadText: Option[String] = weightedRandom(headToHeadTexts)

  private def templateAttributes = Map(
    "headToHeadResult" -> values.headToHeadResult
  ) ++
    teamWithDeclensions("winner", values.winner, lang) ++
    teamWithDeclensions("homeTeam", values.home, lang) ++
    teamWithDeclensions("awayTeam", values.away, lang)
}
