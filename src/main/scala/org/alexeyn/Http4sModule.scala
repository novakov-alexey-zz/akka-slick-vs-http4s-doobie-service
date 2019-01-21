package org.alexeyn

import cats.effect.IO
import com.softwaremill.macwire.wire
import cats.implicits._
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import org.alexeyn.dao.DoobieTripDao
import org.alexeyn.http4s.{CommandRoutes, QueryRoutes}
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.Router

class Http4sModule(cfg: Config) extends StrictLogging {
  //val db = ??? //Database.forConfig("storage", cfg)

  val dao = wire[DoobieTripDao]
  val service = wire[TripService[IO]]
  val apiPrefix = "/api/v1/trips"
  val routes: HttpRoutes[IO] = Router(apiPrefix -> ( wire[QueryRoutes].routes <+> wire[CommandRoutes].routes))

  def init(): IO[Unit] = dao.createSchema()

  //def close(): Unit = stubDao.close()
}
