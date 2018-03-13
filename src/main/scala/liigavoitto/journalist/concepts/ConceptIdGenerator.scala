package liigavoitto.journalist.concepts

import liigavoitto.scores.{Match, Team}

import scala.util.Properties

class ConceptIdGenerator(seriesId: String, mtch: Match) {

  lazy val home = mtch.teams(0)
  lazy val away = mtch.teams(1)
  lazy val homeConcept = teamConceptId(home)
  lazy val awayConcept = teamConceptId(away)

  def teamConceptId(team: Team) = team.meta.directives.map(_.find(_._1 == keyName).map(_._2.asInstanceOf[String])).getOrElse(None)
  
  val env = Properties.envOrElse("ENVIRONMENT", "test")
  val keyName = if (env == "prod") "conceptId" else "conceptIdTest"
  
  private def mapByEnv(list: List[Concept]) = list.map(c => if (env == "prod") c.prodId else c.testId)

  private def conceptListBySeriesId(seriesId: String) = seriesId match {
    case "liiga" => Concepts.LiigaCoreConcepts
    case "mestis" => Concepts.MestisCoreConcepts
    case "naisten-liiga" => Concepts.NaistenLiigaCoreConcepts
    case _ => Concepts.FinnishIcehockeyCoreConcepts
  }

  def getIds: List[String] = mapByEnv(conceptListBySeriesId(seriesId)) ++ List(homeConcept, awayConcept).flatten
  

}
