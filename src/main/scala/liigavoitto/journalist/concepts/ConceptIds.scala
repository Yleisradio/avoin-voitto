package liigavoitto.journalist.concepts

case class Concept(prodId: String, testId: String)

object Concepts {

  val Urheilu = Concept("18-220090", "18-94924")
  val Jaakiekko = Concept("18-215580", "18-33361")
  val SuomenJaakiekko = Concept("18-36075", "18-33603")
  val MiestenJaakiekko = Concept("18-76550", "18-53257")
  val SMLiiga = Concept("18-169617", "18-127648")
  val Mestis = Concept("18-136628", "18-9653")
  val NaistenLiiga = Concept("18-268734", "18-94372")

  def FinnishIcehockeyCoreConcepts: List[Concept] =
    List(
      Urheilu,
      Jaakiekko,
      SuomenJaakiekko
    )

  def LiigaCoreConcepts: List[Concept] =
    List(
      SMLiiga,
      MiestenJaakiekko
    ) ++ FinnishIcehockeyCoreConcepts

  def MestisCoreConcepts: List[Concept] =
    List(
      Mestis,
      MiestenJaakiekko
    ) ++ FinnishIcehockeyCoreConcepts

  def NaistenLiigaCoreConcepts: List[Concept] =
    List(
      NaistenLiiga
    ) ++ FinnishIcehockeyCoreConcepts
}
