package org.alexeyn.http4s

import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import org.alexeyn.TripService
import org.alexeyn.json.CirceJsonCodecs
import org.http4s.HttpRoutes
import org.http4s.dsl.impl.{IntVar, Root}
import org.http4s.dsl.io._

object OptSort extends OptionalQueryParamDecoderMatcher[String]("sort")
object OptPage extends OptionalQueryParamDecoderMatcher[Int]("page")
object OptPageSize extends OptionalQueryParamDecoderMatcher[Int]("pageSize")

class QueryRoutes(service: TripService[IO]) extends CirceJsonCodecs with StrictLogging {

  val routes = HttpRoutes.of[IO] {
    case GET -> Root / IntVar(id) =>
      val trip = service.select(id)
      //TODO: replace with Logger middleware
      logger.debug("Found trip: {}", trip)
      trip.flatMap(_.fold(NotFound())(Ok(_)))

    case GET -> Root :? OptSort(sort) +& OptPage(page) +& OptPageSize(pageSize) =>
      logger.debug("Select all sorted by '{}'", sort)
      service
        .selectAll(page, pageSize, sort)
        .flatMap(Ok(_))
  }
}
