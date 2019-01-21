package org.alexeyn

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._

import scala.collection.mutable

object Http4sMain extends IOApp with StrictLogging {

//  val cfg: Config = ConfigFactory.load()
//  val db = Database.forConfig("storage", cfg)
  val (server, cfg) =
    AppConfig.load.fold(e => sys.error(s"Failed to load configuration:\n${e.toList.mkString("\n")}"), identity)

  //val service = new TripService[IO](dao)
  val mod = new Http4sModule(cfg)
  val apiV1App = mod.routes.orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(apiV1App)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
