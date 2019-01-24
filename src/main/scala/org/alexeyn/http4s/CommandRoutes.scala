package org.alexeyn.http4s

import cats.implicits._
import cats.effect.Sync
import org.alexeyn.json.CirceJsonCodecs
import org.alexeyn.{CommandResult, Trip, TripService, UserError}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class CommandRoutes[F[_]: Sync](service: TripService[F])(implicit H: HttpErrorHandler[F, UserError])
    extends Http4sDsl[F]
    with CirceJsonCodecs {

  val routes: HttpRoutes[F] = H.handle(HttpRoutes.of[F] {
    case req @ POST -> Root =>
      for {
        trip <- req.as[Trip]
        i <- service.insert(trip)
        resp <- Ok(CommandResult(i))
      } yield resp

    case req @ PUT -> Root / IntVar(id) =>
      for {
        trip <- req.as[Trip]
        i <- service.update(id, trip)
        resp <- Ok(CommandResult(i))
      } yield resp

    case DELETE -> Root / IntVar(id) =>
      for {
        i <- service.delete(id)
        resp <- Ok(CommandResult(i))
      } yield resp
  })
}
