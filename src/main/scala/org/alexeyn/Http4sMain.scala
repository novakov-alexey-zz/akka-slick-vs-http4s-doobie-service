package org.alexeyn

import cats.Applicative
import cats.effect._
import cats.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import fs2.Stream

object Http4sMain extends IOApp {
  val (server, jdbc, _) =
    AppConfig.load.fold(e => sys.error(s"Failed to load configuration:\n${e.toList.mkString("\n")}"), identity)

  override def run(args: List[String]): IO[ExitCode] = stream[IO].compile.drain.as(ExitCode.Success)

  def stream[F[_]: ConcurrentEffect: Applicative](implicit C: ContextShift[F]): Stream[F, ExitCode] =
    for {
      mod <- Stream.eval(new Http4sModule(jdbc).pure[F])
      _ <- Stream.eval(mod.init().adaptError { case e => new RuntimeException("Failed to initialize Trips module", e) })

      apiV1App = mod.routes.orNotFound
      finalHttpApp = Logger(logHeaders = true, logBody = true)(apiV1App)

      exitCode <- BlazeServerBuilder[F]
        .bindHttp(server.port.value, server.host.value)
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
}
