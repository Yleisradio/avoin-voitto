package liigavoitto.journalist.utils

import liigavoitto.util.Logging
import org.scalatest.{FlatSpec, Matchers}

class TemplateRenderingSpec extends FlatSpec with Matchers with Logging with WeightedRandomizer {

  val sampleSize = 1000000
  val maxDeviationThreshold = 0.1

  "TemplateRendering" should "render a template without variables and preserve weight" in {
    val template = Template("This is a template.")
    val result = TemplateRendering.render(template, Map())
    result shouldBe Some(RenderedTemplate(template.template, template.weight))
  }

  it should "render a template with a variable and a custom weight" in {
    val template = Template("This is a {{template}}.", 0.5)
    val attr = Map("template" -> "text")
    val result = TemplateRendering.render(template, attr)
    result shouldBe Some(RenderedTemplate("This is a text.", 0.5))
  }

  "WeightedRandomizer" should "always pick from a list of 1" in {
    val templateList = List(RenderedTemplate("test", 1.0))
    val result = weightedRandom(templateList)
    result shouldBe Some("test")
  }

  it should "return None with an empty list" in {
    weightedRandom(List()) shouldBe None
  }

  it should "return equal amounts with default weights" in {
    def getRandom = {
      weightedRandom(List(
        RenderedTemplate("first", 1.0),
        RenderedTemplate("second", 1.0),
        RenderedTemplate("third", 1.0)
      ))
    }

    val results = (1 to sampleSize).flatMap(_ => getRandom)
    val expected = sampleSize / 3

    results.count(t => t == "first") should beCloseToExpected(expected)
    results.count(t => t == "second") should beCloseToExpected(expected)
    results.count(t => t == "third") should beCloseToExpected(expected)
  }

  it should "return different amounts with weighted templates" in {
    def getRandom = {
      weightedRandom(List(
        RenderedTemplate("first", 1.0),
        RenderedTemplate("second", 0.5)
      ))
    }

    val results = (1 to sampleSize).flatMap(_ => getRandom)
    val expected = sampleSize / 3
    results.count(t => t == "second") should beCloseToExpected(expected)
  }

  it should "work even if the templates are not sorted by weight" in {
    def getRandom = {
      weightedRandom(List(
        RenderedTemplate("first", 1.0),
        RenderedTemplate("second", 0.5),
        RenderedTemplate("third", 1.0),
        RenderedTemplate("fourth", 0.5),
        RenderedTemplate("fifth", 1.0)
      ))
    }

    val results = (1 to sampleSize).flatMap(_ => getRandom)

    // each 1.0 weight template should have 1/4 of the results
    val expected = sampleSize / 4
    results.count(t => t == "first") should beCloseToExpected(expected)
    results.count(t => t == "third") should beCloseToExpected(expected)
    results.count(t => t == "fifth") should beCloseToExpected(expected)

    // second and fourth should have 1/4 combined
    results.count(t => t == "second" || t == "fourth") should beCloseToExpected(expected)
  }

  def beCloseToExpected(expected: Double) = {
    val lowerBound = (expected - (expected * maxDeviationThreshold)).toInt
    val higherBound = (expected + (expected * maxDeviationThreshold)).toInt
    be >= lowerBound and be <= higherBound
  }
}
