package org.alexeyn

import cats.effect.{ContextShift, IO}
import com.softwaremill.macwire.wire
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import doobie.Transactor
import org.alexeyn.data.DoobieTripRepository
import org.alexeyn.http4s.{CommandRoutes, HttpErrorHandler, QueryRoutes, UserHttpErrorHandler}
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.Router

import scala.concurrent.ExecutionContext

class Http4sModule(cfg: JdbcConfig) extends StrictLogging {
  type Effect[A] = IO[A]

  implicit val cs: ContextShift[Effect] = IO.contextShift(ExecutionContext.global)

  val xa = Transactor.fromDriverManager[Effect](cfg.driver.value, cfg.url.value, cfg.user.value, cfg.password.value)
  val repo = wire[DoobieTripRepository[Effect]]
  val service = wire[TripService[Effect]]
  val apiPrefix = "/api/v1/trips"

  implicit val errorHandler: HttpErrorHandler[Effect, UserError] = new UserHttpErrorHandler[Effect]()

  val routes: HttpRoutes[Effect] = Router(apiPrefix -> (wire[QueryRoutes[Effect]].routes <+> wire[CommandRoutes[Effect]].routes))

  def init(): Effect[Unit] =
    repo
      .schemaExists()
      .handleErrorWith { _ =>
        logger.info("Going to create database schema")
        repo.createSchema()
      }
}
