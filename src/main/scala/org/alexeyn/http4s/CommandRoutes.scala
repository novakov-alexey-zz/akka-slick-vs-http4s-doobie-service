package org.alexeyn.http4s

import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import org.alexeyn.json.CirceJsonCodecs
import org.alexeyn.{CommandResult, Trip, TripService}
import org.http4s.HttpRoutes
import org.http4s.dsl.impl.{IntVar, Root}
import org.http4s.dsl.io._

class CommandRoutes(service: TripService[IO]) extends CirceJsonCodecs with StrictLogging {

  val routes = HttpRoutes.of[IO] {
    case req @ POST -> Root =>
      for {
        trip <- req.as[Trip]
        _ = logger.debug("Create new trip '{}'", trip)
        i <- service.insertF(trip)
        resp <- Ok(CommandResult(i))
      } yield resp

    case req @ PUT -> Root / IntVar(id) =>
      for {
        trip <- req.as[Trip]
        _ = logger.debug("Update trip: '{}'", trip)
        i <- service.updateF(id, trip)
        resp <- Ok(CommandResult(i))
      } yield resp

    case DELETE -> Root / IntVar(id) =>
      logger.debug("Delete trip: '{}'", id)
      for {
        i <- service.delete(id)
        resp <- Ok(CommandResult(i))
      } yield resp
  }
}
