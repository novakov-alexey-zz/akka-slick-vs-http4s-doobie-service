package org.alexeyn

import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, MediaTypes}
import org.alexeyn.json.GenericJsonWriter

object RequestsSupport {
  private val apiPrefix = "/api/v1/trips"

  def insertRequest[T](e: T)(implicit w: GenericJsonWriter[T]): HttpRequest = {
    val entity = HttpEntity(MediaTypes.`application/json`, w.toJsonString(e))
    HttpRequest(uri = apiPrefix, method = HttpMethods.POST, entity = entity)
  }

  def selectAllRequest(): HttpRequest =
    HttpRequest(uri = apiPrefix)

  def selectAllRequest(sort: String): HttpRequest =
    HttpRequest(uri = s"$apiPrefix?sort=$sort")

  def selectByRequest(id: Int): HttpRequest =
    HttpRequest(uri = s"$apiPrefix/$id")

  def updateRequest[T](e: T, id: Int)(implicit w: GenericJsonWriter[T]): HttpRequest = {
    val entity = HttpEntity(MediaTypes.`application/json`, w.toJsonString(e))
    HttpRequest(uri = s"$apiPrefix/$id", method = HttpMethods.PUT, entity = entity)
  }

  def deleteRequest[T](id: Int): HttpRequest =
    HttpRequest(uri = s"$apiPrefix/$id", method = HttpMethods.DELETE)
}
