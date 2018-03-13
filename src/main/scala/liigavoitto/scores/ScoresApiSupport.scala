package liigavoitto.scores

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.stream.ActorMaterializer
import liigavoitto.util.Logging
import org.joda.time.format.DateTimeFormat

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{ Failure, Properties, Success, Try }

trait ScoresApiSupport extends Logging {
  implicit val system: ActorSystem
  implicit val ec = system.dispatcher
  implicit val fm = ActorMaterializer()

  val oneHundredMegabytes = 100000000

  val apiUrl = Properties.envOrElse("SCORES_API_URL", "http://scores.api.yle.fi/v0/")
  val scoresAuth = Map[String, String](
    "app_id" -> Properties.envOrElse("SCORES_API_APP_ID", ""),
    "app_key" -> Properties.envOrElse("SCORES_API_APP_KEY", "")
  )
  val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
  val timeout = 15.seconds

  protected def get(url: String) = {
    Try {
      val request = HttpRequest(GET, url)
      log.info("REQUEST: " + request)
      Http().singleRequest(request).map(r => getStr(r))
    } match {
      case Success(s) => s
      case Failure(e) =>
        log.warn(s"Failed to get $url: " + e.getMessage)
        e.printStackTrace()
        throw new RuntimeException("Failure: " + e)
    }
  }

  protected def getStr(r: HttpResponse) = {
    Try {
      val entity = Await.result(r.entity.withSizeLimit(oneHundredMegabytes).toStrict(timeout), timeout)
      entity.data.decodeString("UTF-8")
    } match {
      case Success(s) => s
      case Failure(e) => throw new RuntimeException(s"Scores api failure: " + e.getMessage)
    }
  }
}
