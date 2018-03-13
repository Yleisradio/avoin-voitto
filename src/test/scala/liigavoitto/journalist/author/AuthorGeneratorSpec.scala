package liigavoitto.journalist.author

import org.scalatest.{ FlatSpec, Matchers }

class AuthorGeneratorSpec extends FlatSpec with Matchers {

  val name = "Voitto-robotti"
  val author = AuthorGenerator.getAuthor

  "AuthorGenerator" should "give predefined values" in {
    author.id shouldEqual ""
    author.name shouldEqual name
    author.organization shouldEqual "Yle"
  }
}
