package liigavoitto.journalist.lead

import liigavoitto.journalist.text.CommonImplicits
import liigavoitto.journalist.utils.{Template}

trait TeamStreakText extends CommonImplicits {

  private val filePath = "template/lead/team-streak-text.edn"
  private def tmpl = getTemplateFn(filePath)

  def teamStreakTemplates: List[Template] =
    if (values.winnerWinStreak >= 2) tmpl("winning-streak-continues") // winner: wins in row count at least 3
    else if (values.winnerPoints(3) >= 6 && values.significantStandingsRise) tmpl("pointstreak-continues-rise-in-standings") // winner: three last games at least 8 points, new standing > old standing
    else if (values.winnerPoints(3) >= 6) tmpl("pointstreak-continues") // winner: three last games at least 8 points (without rise in standings)
    else if (values.loserLossStreak >= 2) tmpl("losing-streak-continues") // loser: at least 3 losses in a row
    else if (values.winnerLossStreak >= 3) tmpl("losing-streak-ended") // winner: at least 3 previous games have been losses
    else if (values.loserWinStreak >= 3) tmpl("winning-streak-ended") // loser: at least 3 previous matches have been wins
    else if (!values.isPlayoffs && values.firstGoalInShootout) tmpl("no-streak-and-win-in-shootout-after-no-goals") // Use special lead if first goal is in shootout
    else tmpl("no-streak") // just tell the result

  def teamStreakTexts = teamStreakTemplates.flatMap(t => render(t, templateAttributes))

  def teamStreakText = weightedRandom(teamStreakTexts)

  private def templateAttributes = Map(
      "result" -> values.result,
      "winnerResult" -> values.winnerResult,
      "loserResult" -> values.loserResult,
      "riseTemplate" -> riseText
    ) ++
    numeralWithDeclensions("winnerWinStreakPrevious", values.winnerWinStreak, lang) ++ // streak for previous matches
    numeralWithDeclensions("winnerLossStreakPrevious", values.winnerLossStreak, lang) ++
    numeralWithDeclensions("loserWinStreakPrevious", values.loserWinStreak, lang) ++
    numeralWithDeclensions("loserLossStreakPrevious", values.loserLossStreak, lang) ++
    numeralWithDeclensions("winnerWinStreakCurrent", values.winnerWinStreak + 1, lang) ++ // including current match
    numeralWithDeclensions("loserLossStreakCurrent", values.loserLossStreak + 1, lang) ++
    teamWithDeclensions("home", values.home, lang) ++
    teamWithDeclensions("away", values.away, lang) ++
    teamWithDeclensions("winner", values.winner, lang) ++
    teamWithDeclensions("loser", values.loser, lang) ++
    helperDeclensions("homeOrAwayWin", if (values.homeTeamWin) "home-win" else "away-win", lang) ++
    helperDeclensions("winnerHomeOrAway", if (values.homeTeamWin) "home" else "away", lang) ++
    helperDeclensions("loserHomeOrAway", if (!values.homeTeamWin) "home" else "away", lang)

  // Rise attributes and text not in use yet.
  private def riseAttributes = Map(
    "standingsPosition" -> values.standingsPosition,
    "pointsToFirst" -> values.standingsPointsToFirst,
    "leagueLeader" -> values.bestTeam
  )

  private def riseText = {
    val template = {
      if ("rise to first spot, which it wasn't before" == false) tmpl("rise-to-leader")
      else if ("rise over the playoff line" == false) tmpl("rise-over-playoff-line")
      else if ("rise in standings" == false) tmpl("rise-in-standings")
      else if ("points to leader has decreased, and is under x amount" == false) tmpl("rise-to-points-to-leader")
      else List()
    }
    if (!template.isEmpty) render(template.head, riseAttributes).head.text else ""
  }
}
