package liigavoitto.journalist.body

import liigavoitto.journalist.text.CommonImplicits
import liigavoitto.journalist.utils.Template

trait PlacementText extends CommonImplicits {

  private val filePath = "template/body/placement.edn"
  private def tmpl = getTemplateFn(filePath)

  lazy val enoughMatchesPlayed = values.previousHomeTeamMatches.length >= 5 && values.previousAwayTeamMatches.length >= 5

  def placementText: Option[String] = {
    if (!values.teamsInSamePlacementGroup) {
      for {
        homeText <- homeTeamPlacementText
        awayText <- awayTeamPlacementText
      } yield homeText + ", " + awayText
    } else samePlacementGroupText
  }

  def homeTeamPlacementTexts = homeTeamPlacementTemplates.flatMap(t => render(t, templateAttributes))
  def awayTeamPlacementTexts = awayTeamPlacementTemplates.flatMap(t => render(t, templateAttributes))
  def samePlacementGroupTexts = samePlacementGroupTemplates.flatMap(t => render(t, templateAttributes))

  def homeTeamPlacementText = weightedRandom(homeTeamPlacementTexts)
  def awayTeamPlacementText = weightedRandom(awayTeamPlacementTexts)
  def samePlacementGroupText = weightedRandom(samePlacementGroupTexts)

  def samePlacementGroupTemplates: List[Template] = {
    if (enoughMatchesPlayed && values.teamsInSamePlacementGroup) {
      if (values.homeTeamPlacementGroup == values.TeamPlacementGroup.Top)
        tmpl("both-team-top-placement")
      else if (values.homeTeamPlacementGroup == values.TeamPlacementGroup.Mid)
        tmpl("both-team-mid-placement")
      else
        tmpl("both-team-low-placement")
    } else List()
  }

  def homeTeamPlacementTemplates: List[Template] = {
    if (enoughMatchesPlayed && !values.teamsInSamePlacementGroup) {
      if (values.homeTeamPlacementGroup == values.TeamPlacementGroup.First)
        if (values.homeTeamWin) tmpl("home-team-first-place-win") else tmpl("home-team-first-place-loss")
      else if (values.homeTeamPlacementGroup == values.TeamPlacementGroup.Top)
        if (values.homeTeamWin) tmpl("home-team-top-win") else tmpl("home-team-top-loss")
      else if (values.homeTeamPlacementGroup == values.TeamPlacementGroup.Mid)
        tmpl("home-team-mid-placement")
      else if (values.homeTeamWin)
        tmpl("home-team-low-placement-win")
      else
        tmpl("home-team-low-placement-loss")
    } else List()
  }

  def awayTeamPlacementTemplates: List[Template] = {
    if (enoughMatchesPlayed && !values.teamsInSamePlacementGroup) {
      if (values.awayTeamPlacementGroup == values.TeamPlacementGroup.First)
        if (!values.homeTeamWin) tmpl("away-team-first-place-win") else tmpl("away-team-first-place-loss")
      else if (values.awayTeamPlacementGroup == values.TeamPlacementGroup.Top)
        if (!values.homeTeamWin) tmpl("away-team-top-win") else tmpl("away-team-top-loss")
      else if (values.awayTeamPlacementGroup == values.TeamPlacementGroup.Mid)
        tmpl("away-team-mid-placement")
      else if (!values.homeTeamWin)
        tmpl("away-team-low-placement-win")
      else
        tmpl("away-team-low-placement-loss")
    } else List()
  }

  private def templateAttributes =
    teamWithDeclensions("home", values.home, lang) ++
    teamWithDeclensions("away", values.away, lang)
}
