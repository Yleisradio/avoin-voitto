package robottijournalismi.service.journalist.utils

import liigavoitto.journalist.utils.Mustache
import liigavoitto.journalist.utils.Mustache.ValueNotFoundException
import org.scalatest.{ FlatSpec, Matchers }

class MustacheSpec extends FlatSpec with Matchers {

  "Mustache" should "fail rendering when value is not found" in {
    val ctx = Map("something" -> "yes")
    assertThrows[ValueNotFoundException] {
      Mustache("testing {{something}}, {{eiole}}").apply(ctx)
    }
  }

  it should "render a tag with ':' in it" in {
    val ctx = Map("winner:n" -> "voittajan")
    Mustache("Löysin {{winner:n}}.").apply(ctx) shouldEqual "Löysin voittajan."
  }

}
