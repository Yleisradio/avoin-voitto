package liigavoitto.journalist.body

import liigavoitto.journalist.text.CommonImplicits
import liigavoitto.journalist.utils.Template

trait GameProgressTexts extends CommonImplicits {

  private val filePath = "template/body/game-progress.edn"
  private def tmpl = getTemplateFn(filePath)
  lazy val OvertakeInSecondPeriod = tmpl("overtake-in-second-period")
  lazy val TiedInSecondPeriod = tmpl("tied-in-second-period")

  def gameStartTemplates: List[Template] = {
    if (values.awayFirstPeriodGoalDiff >= 3) tmpl("away-team-big-start")
    else if (values.homeFirstPeriodGoalDiff >= 3) tmpl("home-team-big-start")
    else if (values.firstPeriodTotalGoals >= 4) tmpl("many-goals")
    else if (values.firstGoalWithinTwoMinutes) tmpl("flash-start")
    else if (values.firstGoalInSecondPeriod)
      if (values.firstPeriodEqualSaves) tmpl("first-goal-in-second-period-with-equal-saves-in-first")
      else tmpl("first-goal-in-second-period")
    else if (values.firstGoalAfterFifteenMinutesInFirstPeriod) tmpl("slow-start")
    else if (values.tiedAfterFirstPeriodWithGoals) 
      if (values.firstPeriodTeamWithMostShots.isDefined)
        if (values.firstPeriodHasBigShotDifference) tmpl("tied-after-first-period-with-goals-big-shot-difference")
        else  tmpl("tied-after-first-period-with-goals")
      else tmpl("tied-after-first-period-with-goals-and-same-shots")
    else if (values.firstPeriodTotalGoals > 1) tmpl("first-period-default-with-more-than-one-goal")
    else if (values.firstPeriodTotalGoals == 1) tmpl("first-period-default-with-one-goal")
    else List()
  } 
  
  def gameStartTexts = gameStartTemplates.flatMap(render(_, templateAttributes))
  def gameStartText = weightedRandom(gameStartTexts)
  
  def midGameTemplates: List[Template] = 
   if (values.teamOvertakingInSecondPeriod.isDefined) OvertakeInSecondPeriod
   else if (values.teamTyingInSecondPeriod.isDefined) TiedInSecondPeriod
   else if (values.tiedInFirstPeriodAndTakeoverInSecond)
    if (values.secondPeriodLeadWithOne) tmpl("tied-in-first-period-and-takeover-in-second-one-goal-lead")
    else tmpl("tied-in-first-period-and-takeover-in-second")
   else if (values.tiedAfterFirstPeriod && values.tiedAfterSecondPeriod && values.firstPeriodTotalGoals > 0)
     if (values.secondPeriodWithoutGoals) tmpl("tied-in-first-period-and-tied-in-second-period-without-goals")
     else tmpl("tied-in-first-period-and-tied-in-second-period-with-goals")
   else List()
  
  def midGameTexts = midGameTemplates.flatMap(render(_, templateAttributes))
  def midGameText = weightedRandom(midGameTexts)
  
  def lateGameTemplates: List[Template] =
    if (values.loserCatchedUpFromThreeGoalsToOne) tmpl("loser-catched-up-from-three-goals-to-one")
    else if (values.winnerCatchedUpAndWonInRegulation) tmpl("won-after-a-three-goal-catch-up")
    else if (values.winnerCatchedUpAndWonInOvertime) tmpl("won-after-a-three-goal-catch-up-in-overtime")
    else if (values.winnerCatchedUpAndWonInShootout) tmpl("won-after-a-three-goal-catch-up-in-shootout")
    else if (values.firstGoalInOvertime)
      if (values.firstGoalFirstAssistPlayer.isDefined && values.firstGoalSecondAssistPlayer.isDefined) tmpl("first-goal-in-overtime-with-assists")
      else if (values.firstGoalFirstAssistPlayer.isDefined) tmpl("first-goal-in-overtime-with-assist")
      else tmpl("first-goal-in-overtime")
    else if (values.firstGoalInShootout) tmpl("first-goal-in-shootout")
    else if (values.loserTiedWithLastGoalInThirdPeriod)
      if (values.wonInShootout) tmpl("loser-tied-with-last-goal-in-third-period-and-lost-in-shootout")
      else tmpl("loser-tied-with-last-goal-in-third-period-and-lost-in-overtime")
    else if (values.loserTiedInThirdPeriod) tmpl("loser-tied-in-third-period-and-lost")
    else if (values.isShutoutWithThreeOrMoreGoals) tmpl("over-three-goals-to-zero")
    else if (values.winnerInLeadTheWholeGame) tmpl("winner-in-lead-the-whole-game")
    else if (values.teamTyingInSecondPeriodLoses)
      if (values.wonInRegulation) tmpl("team-tying-in-second-period-loses-in-regulation")
      else if (values.wonInOvertime) tmpl("team-tying-in-second-period-loses-in-overtime")
      else tmpl("team-tying-in-second-period-loses-in-shootout")
    else if (values.teamOvertakingInSecondPeriodLoses)
      if (values.wonInRegulation) tmpl("team-overtaking-in-second-period-loses-in-regulation")
      else if (values.wonInOvertime) tmpl("team-overtaking-in-second-period-loses-in-overtime")
      else tmpl("team-overtaking-in-second-period-loses-in-shootout")
    else if (values.goalSpreeInThirdPeriod) tmpl("goal-spree-in-third-period")
    else List()

  def playoffLateGameTemplates: List[Template] =
    if (values.loserCatchedUpFromThreeGoalsToOne) tmpl("loser-catched-up-from-three-goals-to-one")
    else if (values.winnerCatchedUpAndWonInRegulation) tmpl("won-after-a-three-goal-catch-up")
    else if (values.winnerCatchedUpAndWonInAdditionalPeriod) tmpl("won-after-a-three-goal-catch-up-in-additional-period")
    else if (values.firstGoalInAdditionalPeriod) tmpl("first-goal-in-additional-period")
    else if (values.loserTiedWithLastGoalInThirdPeriod) tmpl("loser-tied-with-last-goal-in-third-period-and-lost-in-additional-period")
    else if (values.loserTiedInThirdPeriod) tmpl("loser-tied-in-third-period-and-lost")
    else if (values.isShutoutWithThreeOrMoreGoals) tmpl("over-three-goals-to-zero")
    else if (values.winnerInLeadTheWholeGame) tmpl("winner-in-lead-the-whole-game")
    else if (values.teamTyingInSecondPeriodLoses)
      if (values.hasAdditionalPeriods) tmpl("team-tying-in-second-period-loses-in-additional-period")
      else tmpl("team-tying-in-second-period-loses-in-regulation")
    else if (values.teamOvertakingInSecondPeriodLoses)
      if (values.hasAdditionalPeriods) tmpl("team-overtaking-in-second-period-loses-in-additional-period")
      else tmpl("team-overtaking-in-second-period-loses-in-regulation")
    else if (values.goalSpreeInThirdPeriod) tmpl("goal-spree-in-third-period")
    else if (values.hasAdditionalPeriods) tmpl("won-in-additional-period")
    else List()

  def lateGameTexts = (if (values.isPlayoffs) playoffLateGameTemplates else lateGameTemplates).flatMap(render(_, templateAttributes))
  def lateGameText = weightedRandom(lateGameTexts)
  
  private lazy val templateAttributes = Map(
    "firstPeriodResult" -> values.firstPeriodResult,
    "secondPeriodResult" -> values.secondPeriodResult,
    "result" -> values.result,
    "homeFirstPeriodGoalDiff" -> values.homeFirstPeriodGoalDiff,
    "loserGoalDifferenceAfterSecond" -> values.loserSecondPeriodGoalDiff,
    "firstGoalTime" -> values.firstGoalTime,
    "firstGoalTimeMinutes" -> values.firstGoalTimeMinutes,
    "firstGoalType:text" -> goalTypeText(values.firstGoalType),
    "secondPeriodOvertakingGoalTime" -> values.secondPeriodOvertakingGoalTime,
    "secondPeriodTyingGoalTime" -> values.secondPeriodTyingGoalTime,
    "secondPeriodLastTyingGoalTime" -> values.secondPeriodLastTyingGoalTime,
    "winningGoalTime" -> values.winningGoalTime
  ) ++
    numeralWithDeclensions("winnerBiggestGoalLead", values.winnerBiggestGoalLead, lang) ++
    numeralWithDeclensions("loserBiggestGoalLead", values.loserBiggestGoalLead, lang) ++
    numeralWithDeclensions("firstPeriodTeamWithMostShots.firstPeriodTotalShots", values.firstPeriodTeamWithMostShotsTotalShots, lang) ++
    numeralWithDeclensions("firstPeriodTeamWithLessShots.firstPeriodTotalShots", values.firstPeriodTeamWithLessShotsTotalShots, lang) ++
    numeralWithDeclensions("home.firstPeriodTotalShots", values.firstPeriodHomeTotalShots, lang) ++
    numeralWithDeclensions("firstPeriodTotalGoals", values.firstPeriodTotalGoals, lang) ++
    numeralWithDeclensions("secondPeriodTotalGoals", values.secondPeriodTotalGoals, lang) ++
    numeralWithDeclensions("secondPeriodGoalDiff", values.secondPeriodGoalDiff, lang) ++
    numeralWithDeclensions("goalDifference", values.goalDif, lang) ++
    numeralWithDeclensions("lastAdditionalPeriodNumber", values.lastAdditionalPeriodNumber, lang) ++
    teamWithDeclensions("home", values.home, lang) ++
    teamWithDeclensions("away", values.away, lang) ++
    teamWithDeclensions("winner", values.winner, lang) ++
    teamWithDeclensions("loser", values.loser, lang) ++
    teamAndOpponentWithDeclensions("firstGoalTeam", values.firstGoalTeam, lang) ++
    teamAndOpponentWithDeclensions("firstPeriodLosingTeam", values.firstPeriodLosingTeam, lang) ++
    teamAndOpponentWithDeclensions("firstPeriodLeadingTeam", values.firstPeriodLeadingTeam, lang) ++
    teamAndOpponentWithDeclensions("firstPeriodTeamWithMostShots", values.firstPeriodTeamWithMostShots, lang) ++
    teamAndOpponentWithDeclensions("firstPeriodTeamWithLessShots", values.firstPeriodTeamWithLessShots, lang) ++
    teamAndOpponentWithDeclensions("firstPeriodGoalieWithMostSavesTeam", values.firstPeriodGoalieWithMostSavesTeam, lang) ++
    teamAndOpponentWithDeclensions("firstPeriodGoalieWithSecondMostSavesTeam", values.firstPeriodGoalieWithSecondMostSavesTeam, lang) ++
    teamAndOpponentWithDeclensions("secondPeriodOvertakingTeam", values.teamOvertakingInSecondPeriod, lang) ++
    teamAndOpponentWithDeclensions("secondPeriodLeadingTeam", values.secondPeriodLeadingTeam, lang) ++
    playerWithDeclensions("firstGoalPlayer", values.firstGoalPlayer) ++
    playerWithDeclensions("secondPeriodTyingGoalPlayer", values.secondPeriodTyingGoalPlayer) ++
    playerWithDeclensions("secondPeriodOvertakingGoalPlayer", values.secondPeriodOvertakingGoalPlayer) ++
    playerWithDeclensions("winningGoalPlayer", values.winningGoalPlayer) ++
    playerWithDeclensions("firstPeriodGoalieWithMostSaves", values.firstPeriodGoalieWithMostSaves) ++
    playerWithDeclensions("firstPeriodGoalieWithSecondMostSaves", values.firstPeriodGoalieWithSecondMostSaves) ++
    feedPlayerWithDeclensions("secondPeriodLastTyingGoalPlayer", values.secondPeriodLastTyingGoalPlayer) ++
    feedPlayerWithDeclensions("lastGoalInThirdPeriodPlayer", values.lastGoalInThirdPeriodPlayer) ++
    feedPlayerWithDeclensions("firstGoalFirstAssistPlayer", values.firstGoalFirstAssistPlayer) ++
    feedPlayerWithDeclensions("firstGoalSecondAssistPlayer", values.firstGoalSecondAssistPlayer) ++
    feedPlayerWithDeclensions("lastTyingGoalInThirdPeriodPlayer", values.lastTyingGoalInThirdPeriodPlayer) ++
    goalieStatsTemplateAttributes("firstPeriodGoalieWithMostSaves", values.firstPeriodGoalieWithMostSaves, lang) ++
    goalieStatsTemplateAttributes("firstPeriodGoalieWithSecondMostSaves", values.firstPeriodGoalieWithSecondMostSaves, lang) ++
    helperDeclensions("firstPeriodWinningTeamHomeOrAway", if (values.firstPeriodWinningTeamIsHome) "home-team" else "away-team", lang) ++
    helperDeclensions("firstPeriodLosingTeamHomeOrAway", if (!values.firstPeriodWinningTeamIsHome) "home-team" else "away-team", lang) ++
    helperDeclensions("winnerTeamHomeOrAway", if (values.homeTeamWin) "home-team" else "away-team", lang) ++
    helperDeclensions("loserTeamHomeOrAway", if (!values.homeTeamWin) "home-team" else "away-team", lang) ++
    helperDeclensions("firstGoalTeamHomeOrAway", if (values.firstGoalTeamIsHome) "home-team" else "away-team", lang)
  
  private def goalTypeText(goalType: String) = {
    if (goalType.contains("YV")) tmpl("on-powerplay").head.template + " "
    else if (goalType.contains("AV")) tmpl("on-short-handed").head.template + " "
    else if (goalType == "VL") tmpl("on-penalty-shot").head.template + " "
    else ""
  }

}
