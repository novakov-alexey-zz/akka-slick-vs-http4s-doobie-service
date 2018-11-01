package org.alexeyn

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}

trait JsonCodes extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val vehicle: RootJsonFormat[Vehicle.Value] = enumFormat(Vehicle)

  implicit val localDate: JsonFormat[LocalDate] =
    new JsonFormat[LocalDate] {
      private val formatter = DateTimeFormatter.ISO_DATE
      override def write(x: LocalDate): JsValue = JsString(x.format(formatter))

      override def read(value: JsValue): LocalDate = value match {
        case JsString(x) => LocalDate.parse(x)
        case x => throw DeserializationException(s"Wrong time format of $x")
      }
    }

  // Generic Enumeration formatter
  implicit def enumFormat[T <: Enumeration](implicit enu: T): RootJsonFormat[T#Value] =
    new RootJsonFormat[T#Value] {
      def write(obj: T#Value): JsValue = JsString(obj.toString)
      def read(json: JsValue): T#Value = {
        json match {
          case JsString(txt) => enu.withName(txt)
          case somethingElse =>
            throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
        }
      }
    }

  implicit val trip: RootJsonFormat[Trip] = jsonFormat7(Trip)
  implicit val trips: RootJsonFormat[Trips] = jsonFormat1(Trips)
  implicit val commandResult: RootJsonFormat[CommandResult] = jsonFormat1(CommandResult)
}
