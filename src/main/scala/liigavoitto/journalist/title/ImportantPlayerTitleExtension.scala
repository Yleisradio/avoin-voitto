package liigavoitto.journalist.title

import liigavoitto.journalist.text.CommonImplicits
import liigavoitto.journalist.title.template.BestPlayerTemplates
import liigavoitto.journalist.utils._

trait ImportantPlayerTitleExtension
  extends CommonImplicits
    with TemplateUtils
    with BestPlayerTemplates {
  implicit val lang: String

  def getImportantPlayerExtensions: List[RenderedTemplate] = {
    getImportantPlayerExtensionTemplates.flatMap(t => render(t, importantPlayerTemplateAttributes))
  }

  def getImportantPlayerExtensionTemplates: List[Template] = {
    if (values.bestPlayerStats.goals >= 4) ScoreOverThreeGoals
    else if (values.bestPlayerStats.goals == 3) ScoreHatrick
    else if (values.isShutout && values.winningTeamBestGoalieStats.saves >= 30) GoalieShutout
    else if (values.winningTeamBestPlayerStats.points >= 3)
      if (values.winningTeamBestPlayerTookPartInMostGoals) KeyPlayer
      else WinningTeamPlayerThreeOrMorePoints
    else if (values.losingTeamBestPlayerStats.points >= 3) LosingTeamPlayerThreeOrMorePoints
    else List()
  }

  private def importantPlayerTemplateAttributes: Map[String, Any] =
    teamWithDeclensions("winner", values.winner, lang) ++
    teamWithDeclensions("loser", values.loser, lang) ++
    playerTemplateAttributes("bestPlayer", values.bestPlayer, lang) ++
    playerTemplateAttributes("winningTeamBestPlayer", values.winningTeamBestPlayer, lang) ++
    playerTemplateAttributes("losingTeamBestPlayer", values.losingTeamBestPlayer, lang) ++
    playerTemplateAttributes("winningTeamBestGoalie", values.winningTeamBestGoalie, lang) ++
    playerStatsTemplateAttributes("bestPlayer", values.bestPlayer, lang) ++
    playerStatsTemplateAttributes("winningTeamBestPlayer", values.winningTeamBestPlayer, lang) ++
    playerStatsTemplateAttributes("losingTeamBestPlayer", values.losingTeamBestPlayer, lang) ++
    playerStatsTemplateAttributes("winningTeamBestGoalie", values.winningTeamBestGoalie, lang)
}
