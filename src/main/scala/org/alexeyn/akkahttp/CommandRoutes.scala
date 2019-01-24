package org.alexeyn.akkahttp

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import org.alexeyn._
import org.alexeyn.json.GenericJsonWriter

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class CommandRoutes(service: TripService[Future])(
  implicit ec: ExecutionContext,
  system: ActorSystem,
  w: GenericJsonWriter[CommandResult],
  t: FromRequestUnmarshaller[Trip]
) extends ApiV1
    with CORSHandler {

  def routes: Route = {
    lazy val log = Logging(system, getClass)

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
              toCommandResponse(deleted, CommandResult)
            }
          })
        }
      )
    }
    corsHandler(route)
  }

  private def toCommandResponse[T](
    count: Future[Int],
    f: Int => T
  )(implicit w: GenericJsonWriter[T], ec: ExecutionContext): Future[HttpResponse] =
    count.transformWith {
      case Success(i) =>
        val e = HttpEntity(ContentTypes.`application/json`, w.toJsonString(f(i)))
        Future.successful(HttpResponse(StatusCodes.OK, entity = e))

      case Failure(e) =>
        val (status, msg) = e match {
          case InvalidTrip(trip, m) => (StatusCodes.PreconditionFailed, s"Invalid trip: $trip. Reason: $m")
          case throwable => (StatusCodes.InternalServerError, s"Something bad happened: $throwable")
        }
        Future.successful(HttpResponse(status, entity = msg))
    }
}
