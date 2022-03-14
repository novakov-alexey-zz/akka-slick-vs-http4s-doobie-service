package org.alexeyn.json

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.alexeyn.{CommandResult, Trip, Trips, Vehicle}

trait CirceJsonCodecs {
  implicit val vehicleEncoder: Encoder[Vehicle.Value] = Encoder.encodeEnumeration(Vehicle)
  implicit val vehicleDecoder: Decoder[Vehicle.Value] = Decoder.decodeEnumeration(Vehicle)

  implicit val tripEncoder: Encoder.AsObject[Trip] = deriveEncoder[Trip]
  implicit val tripDecoder: Decoder[Trip] = deriveDecoder[Trip]

  implicit val tripsEncoder: Encoder.AsObject[Trips] = deriveEncoder[Trips]
  implicit val commandResultEncoder: Encoder.AsObject[CommandResult] = deriveEncoder[CommandResult]
}

object CirceJsonCodecs extends CirceJsonCodecs
