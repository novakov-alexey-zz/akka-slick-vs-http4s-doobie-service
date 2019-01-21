package org.alexeyn.json

import cats.effect.IO
import io.circe.{Decoder, Encoder, ObjectEncoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.alexeyn.{CommandResult, Trip, Trips, Vehicle}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

trait CirceJsonCodecs {
  implicit val vehicleEncoder: Encoder[Vehicle.Value] = Encoder.enumEncoder(Vehicle)
  implicit val vehicleDecoder: Decoder[Vehicle.Value] = Decoder.enumDecoder(Vehicle)

  import io.circe.java8.time.decodeLocalDate

  implicit val tripEncoder: ObjectEncoder[Trip] = deriveEncoder[Trip]
  implicit val tripDecoder: Decoder[Trip] =  deriveDecoder[Trip]

  implicit val tripIoEncoder: EntityEncoder[IO, Trip] = jsonEncoderOf[IO, Trip]
  implicit val tripIoDecoder: EntityDecoder[IO, Trip] = jsonOf[IO, Trip]

  implicit val tripsEncoder: ObjectEncoder[Trips] = deriveEncoder[Trips]
  implicit val tripsIoEncoder: EntityEncoder[IO, Trips] = jsonEncoderOf[IO, Trips]

  implicit val commandResultEncoder: ObjectEncoder[CommandResult] = deriveEncoder[CommandResult]
  implicit val commandResultIoEncoder: EntityEncoder[IO, CommandResult] = jsonEncoderOf[IO, CommandResult]
}

object CirceJsonCodecs extends CirceJsonCodecs