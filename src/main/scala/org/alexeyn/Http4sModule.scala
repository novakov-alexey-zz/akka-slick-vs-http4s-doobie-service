package org.alexeyn

import cats.effect.{ContextShift, IO}
import com.softwaremill.macwire.wire
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import doobie.Transactor
import org.alexeyn.dao.DoobieTripDao
import org.alexeyn.http4s.{CommandRoutes, QueryRoutes}
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.Router

import scala.concurrent.ExecutionContext

class Http4sModule(cfg: JdbcConfig) extends StrictLogging {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val xa = Transactor.fromDriverManager[IO](cfg.driver.value, cfg.url.value, cfg.user.value, cfg.password.value)
  val dao = wire[DoobieTripDao[IO]]
  val service = wire[TripService[IO]]
  val apiPrefix = "/api/v1/trips"
  val routes: HttpRoutes[IO] = Router(apiPrefix -> (wire[QueryRoutes].routes <+> wire[CommandRoutes].routes))

  def init(): IO[Unit] =
    dao
      .testDB()
      .handleErrorWith { _ =>
        logger.info("Going to create database schema")
        dao.createSchema()
      }
      .map(_ => ())
}
