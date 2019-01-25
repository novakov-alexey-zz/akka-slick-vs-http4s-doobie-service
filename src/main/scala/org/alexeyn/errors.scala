package org.alexeyn

sealed trait UserError extends Exception
case class InvalidTrip(trip: Trip, msg: String) extends UserError
