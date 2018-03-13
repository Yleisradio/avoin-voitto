package liigavoitto.journalist.title

import liigavoitto.journalist.text.CommonImplicits
import liigavoitto.journalist.title.template.TitleTemplates
import liigavoitto.journalist.utils.{Template}

trait PrimaryTitleText extends CommonImplicits with TitleTemplates {

  private def templateAttributes =
    Map(
      "result" -> values.result,
      "winner" -> values.winner.name,
      "loser" -> values.loser.name
    ) ++
    teamWithDeclensions("winner", values.winner, lang) ++
    teamWithDeclensions("loser", values.loser, lang)

  def primaryTitleTexts = primaryTitleTemplates.flatMap(t => render(t, templateAttributes))

  def primaryTitleTemplates = {
    if (values.goalDif >= 4 && values.homeTeamWin) HomeTeamWinOver3
    else if (values.goalDif >= 4) TeamWinOver3
    else if (values.endPeriod == 5) TeamWinInShootouts
    else if (values.endPeriod == 4) TeamWinInOvertime
    else if (values.goalDif < 5 && values.shutout && values.homeTeamWin) HomeTeamWinWithShutout
    else if (values.goalDif < 5 && values.shutout) TeamWinWithShutout
    else if (values.goalDif == 1) TeamWinOnly1Goal
    else if (values.homeTeamWin) HomeTeamWinUnder4InRegular
    else AwayTeamWinUnder4InRegular
  }
}
