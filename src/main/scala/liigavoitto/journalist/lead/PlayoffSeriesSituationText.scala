package liigavoitto.journalist.lead

import liigavoitto.journalist.text.CommonImplicits
import liigavoitto.journalist.utils.{Template}

trait PlayoffSeriesSituationText extends CommonImplicits {

  private val filePath = "template/lead/playoff-series-situation-text.edn"
  private def tmpl = getTemplateFn(filePath)

  def playoffSeriesSituationTemplates: List[Template] =
    if (values.isThirdPlacePlayoff) tmpl("winner-wins-third-place")
    else if (values.isSeriesOpener) {
      if (values.isFinalSeries) tmpl("winner-series-opener-final")
      else tmpl("winner-series-opener-non-final")
    }
    else if (values.isFinalMatch) {
      if (values.isFinalSeries) tmpl("leader-wins-championship")
      else tmpl("leader-wins-series")
    }
    else if (values.winnerLeads) {
      if (values.isFinalSeries) tmpl("leader-wins-final")
      else tmpl("leader-wins-non-final")
    }
    else if (values.winnerEqualised) {
      if (values.isFinalSeries) tmpl("non-leader-equalised-final")
      else tmpl("non-leader-equalised-non-final")
    }
    else if (!values.winnerLeads) {
      if (values.isFinalSeries) tmpl("non-leader-wins-final")
      else tmpl("non-leader-wins-non-final")
    }
    else List()

  def playoffSeriesSituationTexts = playoffSeriesSituationTemplates.flatMap(t => render(t, templateAttributes))

  def playoffSeriesSituationText = weightedRandom(playoffSeriesSituationTexts)

  private def templateAttributes =
    Map(
      "result" -> values.result
    ) ++
    teamWithDeclensions("home", values.home, lang) ++
    teamWithDeclensions("away", values.away, lang) ++
    teamWithDeclensions("winner", values.winner, lang) ++
    teamWithDeclensions("loser", values.loser, lang) ++
    numeralWithDeclensions("winnerCurrentSeriesWins", values.winnerCurrentSeriesWins, lang) ++
    numeralWithDeclensions("loserCurrentSeriesWins", values.loserCurrentSeriesWins, lang) ++
    helperDeclensions("winnerHomeOrAway", if (values.homeTeamWin) "home" else "away", lang)
}
