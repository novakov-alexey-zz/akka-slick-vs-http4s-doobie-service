package org.alexeyn.http

import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.server.Directives._

trait ApiV1 {
  def apiPrefix: Directive[Unit] = pathPrefix("api" / "v1" / "trips")
}
