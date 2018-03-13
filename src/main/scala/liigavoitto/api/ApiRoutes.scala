package liigavoitto.api

import akka.actor.ActorSystem
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.pathPrefix
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.Directives._

/**
  * Routes can be defined in separated classes like shown in here
  */
trait ApiRoutes {
  implicit val system: ActorSystem
  val handler = new ApiHandler()

  val apiRoutes =
    get {
      pathPrefix("report" / Segment) { id => 
        parameters("lang".?) { lang =>
          complete(handler.report(id, lang.getOrElse("fi")))
        }
      }
    }

  val localRoute =
    get {
      pathPrefix("localReport" / Segment) { id =>
        parameters("lang".?) { lang =>
          complete(handler.localReport(id, lang.getOrElse("fi")))
        }
      }
    }
}
