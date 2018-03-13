package liigavoitto.journalist.title

import liigavoitto.journalist.text.CommonImplicits
import liigavoitto.journalist.title.template.TitleTemplates
import liigavoitto.journalist.utils.{Template}

trait PlayoffsTitleText extends CommonImplicits with TitleTemplates {

  private val filePath = "template/title/playoffs-title-templates.edn"
  private def tmpl = getTemplateFn(filePath)

  private def templateAttributes =
    Map(
      "result" -> values.result,
      "winner" -> values.winner.name,
      "loser" -> values.loser.name
    ) ++
    teamWithDeclensions("winner", values.winner, lang) ++
    teamWithDeclensions("loser", values.loser, lang) ++
    helperDeclensions("loserHomeOrAway", if (!values.homeTeamWin) "home" else "away", lang) ++
    numeralWithDeclensions("seriesMatchCount", values.seriesMatchCount, lang)


  def playoffsTitleTexts = playoffsTitleTemplates.flatMap(t => render(t, templateAttributes))

  def playoffsTitleTemplates = {
    if (values.isFinalMatch && values.isFinalSeries) tmpl("winner-won-championship")
    else if (values.isThirdPlacePlayoff) tmpl("winner-won-third-place")
    else if (values.isFinalMatch) tmpl("winner-won-series")
    else if (values.winnerEqualised) tmpl("winner-equalised")
    else if (values.winnerOneWinAwayFromSeriesWin) tmpl("winner-one-win-away")
    else if (values.winnerTookTheLead) tmpl("winner-took-the-lead")
    else tmpl("winner-won")
  }
}
