package org.alexeyn.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import org.alexeyn._
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

object CommandRoutes extends JsonCodes with ApiV1 with CORSHandler {

  def routes(service: TripService[Future])(implicit ec: ExecutionContext, system: ActorSystem): Route = {
    lazy val log = Logging(system, CommandRoutes.getClass)

    val route = apiPrefix {
      concat(
        pathEndOrSingleSlash {
          post {
            entity(as[Trip]) { trip =>
              log.debug("Create new trip '{}'", trip)
              val inserted = service.insert(trip)
              complete {
                toCommandResponse(inserted, CommandResult)
              }
            }
          }
        },
        path(IntNumber) { id =>
          concat(put {
            entity(as[Trip]) { trip =>
              log.debug("Update trip: '{}'", trip)
              val updated = service.update(id, trip)
              complete {
                toCommandResponse(updated, CommandResult)
              }
            }
          })
        },
        path(IntNumber) { id =>
          concat(delete {
            log.debug("Delete trip: '{}'", id)
            val deleted = service.delete(id)
            complete {
              toCommandResponse(Right(deleted), CommandResult)
            }
          })
        }
      )
    }
    corsHandler(route)
  }

  private def toCommandResponse[T](
    count: Either[String, Future[Int]],
    f: Int => T
  )(implicit ev: JsonWriter[T], ec: ExecutionContext): Future[HttpResponse] = {

    count match {
      case Right(c) =>
        c.map(i => {
          val entity = HttpEntity(ContentTypes.`application/json`, f(i).toJson.toString())
          HttpResponse(StatusCodes.OK, entity = entity)
        })
      case Left(e) =>
        Future.successful(HttpResponse(StatusCodes.PreconditionFailed, entity = e))
    }
  }
}
