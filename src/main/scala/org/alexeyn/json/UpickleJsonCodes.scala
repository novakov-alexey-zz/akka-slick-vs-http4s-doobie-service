package org.alexeyn.json

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import de.heikoseeberger.akkahttpupickle.UpickleSupport
import org.alexeyn.Vehicle.Vehicle
import org.alexeyn.{CommandResult, Trip, Trips, Vehicle}
import upickle.default._

trait UpickleJsonCodes extends UpickleSupport {

  implicit def vehicle: ReadWriter[Vehicle] =
    upickle.default
      .readwriter[String]
      .bimap[Vehicle.Vehicle](v => v.toString, str => Vehicle.withName(str))

  implicit def localDate: ReadWriter[LocalDate] =
    upickle.default
      .readwriter[String]
      .bimap[LocalDate](d => d.format(DateTimeFormatter.ISO_LOCAL_DATE), str => LocalDate.parse(str))

  implicit val tripRW: ReadWriter[Trip] = macroRW
  implicit val tripsRW: ReadWriter[Trips] = macroRW
  implicit val commandResultRW: ReadWriter[CommandResult] = macroRW

  def genericJsonWriter[T: Writer]: GenericJsonWriter[T] = (e: T) => write(e)

  implicit val genericTrip: GenericJsonWriter[Trip] = genericJsonWriter[Trip]
  implicit val genericTrips: GenericJsonWriter[Trips] = genericJsonWriter[Trips]
  implicit val genericCommandResult: GenericJsonWriter[CommandResult] = genericJsonWriter[CommandResult]
}

object UpickleJsonCodes extends UpickleJsonCodes
