package org.alexeyn

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.concat
import cats.instances.future.catsStdInstancesForFuture
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import org.alexeyn.akkahttp.{CommandRoutes, QueryRoutes}
import org.alexeyn.json.SprayJsonCodes._
import slick.jdbc.PostgresProfile.api._
import com.softwaremill.macwire._
import org.alexeyn.dao.SlickTripDao

import scala.concurrent.{ExecutionContext, Future}

class AkkaModule(cfg: Config)(implicit system: ActorSystem, ec: ExecutionContext) extends StrictLogging {

  val db = Database.forConfig("storage", cfg)
  val dao = wire[SlickTripDao]
  val service = wire[TripService[Future]]
  val routes = concat(wire[QueryRoutes].routes, wire[CommandRoutes].routes)

  def init(): Future[Either[Throwable, Unit]] =
    dao.createSchema().failed.map(t => Left(t))

  def close(): Unit = db.close()
}
