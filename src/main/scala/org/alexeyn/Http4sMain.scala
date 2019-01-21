package org.alexeyn

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import org.alexeyn.http4s.{CommandRoutes, QueryRoutes}
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import slick.jdbc.PostgresProfile.api._
import org.http4s.implicits._

import scala.collection.mutable

object Http4sMain extends IOApp with StrictLogging {

  val cfg: Config = ConfigFactory.load()
  val db = Database.forConfig("storage", cfg)

  val dao: Dao[Trip, IO] = new Dao[Trip, IO] {
    var state = mutable.Map[Int, Trip]()
    override def delete(id: Int) = {
      state -= id
      IO.pure(id)
    }
    override def update(id: Int, row: Trip) = {
      state += (id -> row)
      IO.pure(id)
    }
    override def createSchema() = IO.unit
    override def insert(row: Trip) = {
      state += (row.id -> row)
      IO(state.size)
    }
    override def selectAll(page: Int, pageSize: Int, sort: String) = IO(state.values.toSeq)
    override def select(id: Int) = IO(state.get(id))
    override def sortingFields = Set("id")
  }

  val service = new TripService[IO](dao)
  val apiPrefix = "/api/v1/trips"
  val apiV1App = Router(apiPrefix -> (new QueryRoutes(service).routes <+> new CommandRoutes(service).routes)).orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(apiV1App)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
