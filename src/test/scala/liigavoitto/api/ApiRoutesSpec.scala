package liigavoitto.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import org.scalatest.{ Matchers, WordSpec }

class ApiRoutesSpec extends WordSpec with Matchers with ScalatestRouteTest with ApiRoutes {

  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  "SimpleRoute" should {
    "answer to GET requests to `/report/s24-123456`" in {
      Get("/report/s24-123456") ~> apiRoutes ~> check {
        status shouldBe StatusCodes.OK
      }
    }
    
    "handle reports in two languages" in {
      Get("/report/s24-123456?lang=sv") ~> apiRoutes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] should include("lang: sv")
      }
    }
  }

  override val handler = new ApiHandler() {
    override def report(id: String, lang: String) = s"OK, $id. lang: $lang"
  }
}
