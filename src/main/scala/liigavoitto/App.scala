package liigavoitto

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import liigavoitto.api.{ ApiRoutes, BaseRoutes }
import liigavoitto.util.Logging

import scala.util.Properties

object App extends Directives with ApiRoutes with Logging {
  implicit lazy val system = ActorSystem("liiga-voitto")
  lazy val port = Properties.envOrElse("APP_PORT", "45258").toInt

  def main(args: Array[String]) {
    implicit val executionContext = system.dispatcher
    implicit val fm = ActorMaterializer()

    Http().bindAndHandle(routes, "0.0.0.0", port)
    log.info(s"Server online at http://0.0.0.0:$port/")
  }

  val routes = BaseRoutes.baseRoutes ~ apiRoutes ~ localRoute
}
