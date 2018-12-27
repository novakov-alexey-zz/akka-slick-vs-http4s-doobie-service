package org.alexeyn

import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, MediaTypes}
import org.alexeyn.json.GenericJsonWriter

object RequestsSupport {

  def insertRequest[T](e: T)(implicit w: GenericJsonWriter[T]): HttpRequest = {
    val entity = HttpEntity(MediaTypes.`application/json`, w.toJsonString(e))
    HttpRequest(uri = "/api/v1/trips", method = HttpMethods.POST, entity = entity)
  }

  def selectAllRequest(): HttpRequest =
    HttpRequest(uri = "/api/v1/trips")
  
  def selectAllRequest(sort: String): HttpRequest =
    HttpRequest(uri = s"/api/v1/trips?sort=$sort")

  def selectByRequest(id: Int): HttpRequest =
    HttpRequest(uri = s"/api/v1/trips/$id")

  def updateRequest[T](e: T, id: Int)(implicit w: GenericJsonWriter[T]): HttpRequest = {
    val entity = HttpEntity(MediaTypes.`application/json`, w.toJsonString(e))
    HttpRequest(uri = s"/api/v1/trips/$id", method = HttpMethods.PUT, entity = entity)
  }

  def deleteRequest[T](id: Int): HttpRequest =
    HttpRequest(uri = s"/api/v1/trips/$id", method = HttpMethods.DELETE)

}
