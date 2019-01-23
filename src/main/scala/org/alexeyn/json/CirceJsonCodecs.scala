package org.alexeyn.json

import cats.Applicative
import cats.effect.{IO, Sync}
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
  implicit def tripEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Trip] = jsonEncoderOf
  implicit val tripDecoder: Decoder[Trip] = deriveDecoder[Trip]
  implicit def tripEntityDecoder[F[_]: Sync]: EntityDecoder[F, Trip] = jsonOf

  implicit val tripIoEncoder: EntityEncoder[IO, Trip] = jsonEncoderOf[IO, Trip]
  implicit val tripIoDecoder: EntityDecoder[IO, Trip] = jsonOf[IO, Trip]

  implicit val tripsEncoder: ObjectEncoder[Trips] = deriveEncoder[Trips]
  implicit def tripsEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Trips] = jsonEncoderOf
  implicit val tripsIoEncoder: EntityEncoder[IO, Trips] = jsonEncoderOf[IO, Trips]

  implicit val commandResultEncoder: ObjectEncoder[CommandResult] = deriveEncoder[CommandResult]
  implicit def commandResultEntityEncoder[F[_]: Applicative]: EntityEncoder[F, CommandResult] = jsonEncoderOf
  implicit val commandResultIoEncoder: EntityEncoder[IO, CommandResult] = jsonEncoderOf[IO, CommandResult]
}

object CirceJsonCodecs extends CirceJsonCodecs
