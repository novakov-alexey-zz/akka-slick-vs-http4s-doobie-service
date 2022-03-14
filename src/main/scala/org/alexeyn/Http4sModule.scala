package org.alexeyn

import cats.effect.Async
import cats.implicits._
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import doobie.Transactor
import org.alexeyn.data.DoobieTripRepository
import org.alexeyn.http4s.{CommandRoutes, HttpErrorHandler, QueryRoutes, UserHttpErrorHandler}
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.Router

class Http4sModule[F[_]: Async](cfg: JdbcConfig) extends StrictLogging {

  val xa = Transactor.fromDriverManager[F](cfg.driver.value, cfg.url.value, cfg.user.value, cfg.password.value)
  val repo = wire[DoobieTripRepository[F]]
  val service = wire[TripService[F]]
  val apiPrefix = "/api/v1/trips"

  implicit val errorHandler: HttpErrorHandler[F, UserError] = new UserHttpErrorHandler[F]()

  val routes: HttpRoutes[F] = Router(apiPrefix -> (wire[QueryRoutes[F]].routes <+> wire[CommandRoutes[F]].routes))

  def init(): F[Unit] =
    repo
      .schemaExists()
      .handleErrorWith { _ =>
        logger.info("Going to create database schema")
        repo.createSchema()
      }
}
