package liigavoitto.journalist.body

import liigavoitto.journalist.text.CommonImplicits
import liigavoitto.journalist.utils.Template

trait WinPercentageText extends CommonImplicits {

  private val filePath = "template/body/win-percentage.edn"
  private def tmpl = getTemplateFn(filePath)

  def winPercentageTemplates: List[Template] = {
    if (values.winnerTotalMatches >= 5) {
      if (values.winnerWinPercentage >= 65) tmpl("winner-many-wins")
      else if (values.winnerWinPercentage <= 35) tmpl("winner-few-wins")
      else tmpl("fallback")
    } else List()
  }

  def winPercentageTexts = winPercentageTemplates.flatMap(t => render(t, templateAttributes))
  def winPercentageText: Option[String] = weightedRandom(winPercentageTexts)

  private def templateAttributes = Map(
    "winnerTotalMatches" -> values.winnerTotalMatches
  ) ++
    teamOrdinals ++
    teamWithDeclensions("winner", values.winner, lang) ++
    teamWithDeclensions("loser", values.loser, lang)

  private def teamOrdinals = {
    if (values.winnerWins > 0 && values.loserLosses > 0) {
      numeralWithDeclensions("winnerWins", values.winnerWins, lang) ++
      numeralWithDeclensions("loserLosses", values.loserLosses, lang)
    }
    else Map()
  }
}
