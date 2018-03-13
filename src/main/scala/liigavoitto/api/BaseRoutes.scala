package liigavoitto.api

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import liigavoitto.util.Logging

/**
 * Routes can be defined in separated classes like shown in here
 */
object BaseRoutes extends Logging {

  lazy val baseRoutes: Route =
    path("ping") {
      get {
        complete("PONG")
      }
    }
}
