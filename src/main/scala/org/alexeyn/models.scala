package org.alexeyn

import java.time.LocalDate

import org.alexeyn.Vehicle.Vehicle

final case class Trip(
  id: Int,
  city: String,
  vehicle: Vehicle,
  price: Int,
  completed: Boolean,
  distance: Option[Int],
  endDate: Option[LocalDate]
)

final case class Trips(trips: Seq[Trip])

object Vehicle extends Enumeration {
  type Vehicle = Value
  val Bike, Taxi, Car = Value
}

final case class CommandResult(count: Int)
