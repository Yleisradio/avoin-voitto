package liigavoitto.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{ Matchers, WordSpec }

class BaseRoutesSpec extends WordSpec with Matchers with ScalatestRouteTest {

  "BaseRoute" should {
    "answer to /ping request" in {
      Get("/ping") ~> BaseRoutes.baseRoutes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldBe "PONG"
      }
    }
  }

}
