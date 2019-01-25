package org.alexeyn

import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object Http4sMain extends IOApp with StrictLogging {
  val (server, jdbc, cfg) =
    AppConfig.load.fold(e => sys.error(s"Failed to load configuration:\n${e.toList.mkString("\n")}"), identity)

  def stream[F[_]: ConcurrentEffect](implicit C: ContextShift[F]) = {
    lazy val mod = new Http4sModule(jdbc)
    for {
      _ <- mod.init().adaptError { case e => new Exception("Failed to initialize Trips module", e) }

      apiV1App = mod.routes.orNotFound
      finalHttpApp = Logger(logHeaders = true, logBody = true)(apiV1App)

      exitCode <- BlazeServerBuilder[F]
        .bindHttp(server.port.value, server.host.value)
        .withHttpApp(finalHttpApp)
        .serve
        .compile
        .drain
    } yield exitCode
  }

  def run(args: List[String]): IO[ExitCode] = {
    stream[IO].as(ExitCode.Success)
  }
}
