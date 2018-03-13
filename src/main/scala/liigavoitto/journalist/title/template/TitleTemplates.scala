package liigavoitto.journalist.title.template

import liigavoitto.journalist.utils.{Template, TemplateLoader}

trait TitleTemplates extends TemplateLoader {
  implicit val lang: String
  
  def loadTmpl(name: String) = load("template/title/title-templates.edn", name, lang)

  lazy val HomeTeamWinOver3 = loadTmpl("home-team-win-over-3")

  lazy val TeamWinOver3 = loadTmpl("team-win-over-3")

  lazy val HomeTeamWinUnder4InRegular = loadTmpl("home-team-win-under-4-in-regular")

  lazy val AwayTeamWinUnder4InRegular = loadTmpl("away-team-win-under-4-in-regular")

  lazy val TeamWinOnly1Goal = loadTmpl("team-win-only-1-goal")

  lazy val TeamWinInOvertime = loadTmpl("team-win-in-overtime")

  lazy val TeamWinInShootouts = loadTmpl("team-win-in-shootout")

  lazy val HomeTeamWinWithShutout = loadTmpl("home-team-win-with-shutout")

  lazy val TeamWinWithShutout = loadTmpl("team-win-with-shutout")
  
}
