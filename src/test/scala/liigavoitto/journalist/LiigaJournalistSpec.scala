package liigavoitto.journalist

import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

import scala.util.Try

class LiigaJournalistSpec
  extends WordSpecLike
  with TestUtils
  with BeforeAndAfterAll
  with MustMatchers
  with MockData {

  "LiigaJournalist" must {
    "create an article with language" in {
      val data = md

      val finnishRes = LiigaJournalist.createArticle(data, "fi")
      assert(finnishRes.isDefined)
      assert(finnishRes.get.language == "fi")

      val swedishRes = LiigaJournalist.createArticle(data, "sv")
      assert(swedishRes.isDefined)
      assert(swedishRes.get.language == "sv")
    }
  }
}
