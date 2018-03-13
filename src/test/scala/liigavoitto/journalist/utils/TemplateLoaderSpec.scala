package liigavoitto.journalist.utils

import org.scalatest.{FlatSpec, Matchers}

class TemplateLoaderSpec extends FlatSpec with Matchers with TemplateLoader {

  val testData = load("template/loader-test.edn", "score-over-three-goals", "fi")
  "TemplateLoader" should "load templates from an edn file" in {
    testData.length shouldBe 2
    testData.head shouldBe Template("{{bestPlayer}} tykitti {{bestPlayer.goals:text}} maalia")
  }
  
  it should "parse template weight if set" in {
    testData(1).weight shouldBe 0.5
  }
}
