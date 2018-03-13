package liigavoitto.journalist.title.template

import liigavoitto.journalist.utils.TemplateLoader

trait BestPlayerTemplates extends TemplateLoader {
  
  implicit val lang: String 
  private val ednFile = "template/title/important-player-title-extensions.edn"
  private def loadList(name: String) = load(ednFile, name, lang)

  lazy val ScoreOverThreeGoals = loadList("score-over-three-goals") 

  lazy val ScoreHatrick = loadList("score-hatrick")

  lazy val KeyPlayer = loadList("key-player")
  
  lazy val WinningTeamPlayerThreeOrMorePoints = loadList("winning-team-player-three-or-more-points")
  
  lazy val LosingTeamPlayerThreeOrMorePoints = loadList("losing-team-player-three-or-more-points")
    
  lazy val GoalieShutout = loadList("goalie-shutout")
}
