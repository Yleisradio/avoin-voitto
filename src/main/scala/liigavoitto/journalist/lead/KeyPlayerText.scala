package liigavoitto.journalist.lead

import liigavoitto.journalist.text.CommonImplicits
import liigavoitto.journalist.utils.{RenderedTemplate, Template}

trait KeyPlayerText extends CommonImplicits {

  private val filePath = "template/lead/key-player-text.edn"
  private def tmpl = getTemplateFn(filePath)

  def keyPlayerTemplates: List[Template] = {
    val bestPlayerStats = values.winningTeamBestPlayerStats

    if (bestPlayerStats.points >= 3 && values.bestPlayerScoresWinningGoal) {
      if (bestPlayerStats.goals == 3 && values.winner.id == values.home.id) tmpl("winning-goal-hattrick-home") ++ tmpl("winning-goal-hattrick")
      else if (bestPlayerStats.goals == 3 && values.winner.id == values.away.id) tmpl("winning-goal-hattrick-away") ++ tmpl("winning-goal-hattrick")
      else if (bestPlayerStats.assists == 0) tmpl("winning-goal-no-assists")
      else if (bestPlayerStats.assists == 1) tmpl("winning-goal-one-assist")
      else if (bestPlayerStats.goals == 1) tmpl("winning-goal-only-with-assists")
      else tmpl("winning-goal-with-good-points")
    }
    else if (bestPlayerStats.points >= 3 && bestPlayerStats.goals >= 2) {
      if (bestPlayerStats.assists == 0) tmpl("key-player-no-assists")
      else if (bestPlayerStats.assists == 1) tmpl("key-player-one-assist")
      else tmpl("key-player-many-assists")
    }
    else if (values.isShutout && values.winningTeamBestGoalieStats.saves >= 20) tmpl("goalie-shutout")
    else if (bestPlayerStats.points >= 3) tmpl("key-player-points")
    else if (values.goalDif == 1 && values.totalGoals > 2) {
      if (values.isPlayoffs && values.hasAdditionalPeriods) tmpl("playoff-additional-periods")
      else if (values.endPeriod == 5) tmpl("shootout")
      else if (values.endPeriod == 4) tmpl("overtime")
      else tmpl("regulation")
    }
    else List()
  }

  def losingTeamKeyPlayerTexts = losingTeamKeyPlayerTemplates.flatMap(t => render(t, templateAttributes))
  def losingTeamKeyPlayerText = weightedRandom(losingTeamKeyPlayerTexts)
  def losingTeamKeyPlayerTemplates: List[Template] = {
    // If keyPlayerTemplates include overtime, shootout and regulation texts.
    // It means that the winner team doesn't have heroic players.
    val resultTemplates = tmpl("shootout") ++ tmpl("overtime") ++ tmpl("regulation")
    if (keyPlayerTemplates.exists(list => resultTemplates.contains(list))) {
      val bestPlayerStats = values.losingTeamBestPlayerStats
      if (bestPlayerStats.goals >= 3) tmpl("losing-key-player-three-or-more-goals")
      else if (bestPlayerStats.goals >= 2) tmpl("losing-key-player-two-goals")
      else List()
    }
    else List()
  }

  private def withAddition(tmpl: RenderedTemplate): RenderedTemplate =
    losingTeamKeyPlayerText.map(addition => RenderedTemplate(tmpl.text + " " + addition, tmpl.weight)).getOrElse(tmpl)

  def keyPlayerTexts = keyPlayerTemplates.flatMap(t => render(t, templateAttributes).map(withAddition))
  def keyPlayerText = weightedRandom(keyPlayerTexts)

  private def templateAttributes = Map(
    "winningGoalTime" -> values.winningGoalTime
  ) ++
    teamWithDeclensions("home", values.home, lang) ++
    teamWithDeclensions("away", values.away, lang) ++
    teamWithDeclensions("winner", values.winner, lang) ++
    teamWithDeclensions("loser", values.loser, lang) ++
    playerWithDeclensions("winningTeamBestPlayer", values.winningTeamBestPlayer) ++
    playerWithDeclensions("winningGoalPlayer", values.winningGoalPlayer) ++
    playerWithDeclensions("losingTeamBestPlayer", values.losingTeamBestPlayer) ++
    playerWithDeclensions("winningTeamBestGoalie", values.winningTeamBestGoalie) ++
    goalieStatsTemplateAttributes("winningTeamBestGoalie", values.winningTeamBestGoalie, lang) ++
    playerStatsTemplateAttributes("winningTeamBestPlayer", values.winningTeamBestPlayer, lang) ++
    playerStatsTemplateAttributes("losingTeamBestPlayer", values.losingTeamBestPlayer, lang) ++
    helperDeclensions("winnerTeamHomeOrAway", if (values.homeTeamWin) "home-team" else "away-team", lang) ++
    helperDeclensions("loserTeamHomeOrAway", if (!values.homeTeamWin) "home-team" else "away-team", lang) ++
    numeralWithDeclensions("lastAdditionalPeriodNumber", values.lastAdditionalPeriodNumber, lang)
}
