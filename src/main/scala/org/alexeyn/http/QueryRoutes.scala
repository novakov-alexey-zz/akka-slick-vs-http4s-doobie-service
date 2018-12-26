package org.alexeyn.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import org.alexeyn.{JsonCodes, TripService}

import scala.concurrent.Future

object QueryRoutes extends JsonCodes with ApiV1 with CORSHandler {
  def routes(service: TripService[Future])(implicit system: ActorSystem): Route = {
    lazy val log = Logging(system, QueryRoutes.getClass)

    val route = apiPrefix {
      concat(
        pathEndOrSingleSlash {
          get {
            parameters('sort.?, 'page.as[Int].?, 'pageSize.as[Int].?) { (sort, page, pageSize) =>
              log.debug("Select all sorted by '{}'", sort)
              val allTrips = service.selectAll(page, pageSize, sort)
              complete(allTrips)
            }
          }
        },
        path(IntNumber) { id =>
          concat(get {
            val maybeTrip = service.select(id)
            log.debug("Found trip: {}", maybeTrip)
            rejectEmptyResponse {
              complete(maybeTrip)
            }
          })
        }
      )
    }
    corsHandler(route)
  }
}
