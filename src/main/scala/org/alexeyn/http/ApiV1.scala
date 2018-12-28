package org.alexeyn.http

import akka.http.scaladsl.server.Directives.{pathPrefix, _}
import akka.http.scaladsl.server.Route

trait ApiV1 {
  def apiPrefix(r: Route): Route = pathPrefix("api" / "v1" / "trips")(r)
}
