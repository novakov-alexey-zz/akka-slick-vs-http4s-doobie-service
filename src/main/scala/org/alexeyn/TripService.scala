package org.alexeyn

import cats.{Functor, MonadError}
import cats.syntax.functor._
import cats.syntax.flatMap._
import org.alexeyn.TripService._

import scala.language.higherKinds

class TripService[F[_]: Functor](dao: Dao[Trip, F])(implicit M: MonadError[F, Throwable]) {

  def selectAll(page: Option[Int], pageSize: Option[Int], sort: Option[String]): Either[String, F[Trips]] = {
    val sortBy = sort
      .map(s => dao.sortingFields.find(_ == s).toRight(s"Unknown sort field $s"))
      .getOrElse(Right(DefaultSortField))

    val pageN = page.getOrElse(DefaultPage)
    val size = pageSize.getOrElse(DefaultPageSize)

    sortBy.map { sort =>
      dao
        .selectAll(pageN, size, sort)
        .map(Trips)
    }
  }

  def selectAllF(page: Option[Int], pageSize: Option[Int], sort: Option[String]): F[Trips] = {
    val sortBy = sort
      .map(s => dao.sortingFields.find(_ == s).toRight(s"Unknown sort field $s"))
      .getOrElse(Right(DefaultSortField))

    lazy val pageN = page.getOrElse(DefaultPage)
    lazy val size = pageSize.getOrElse(DefaultPageSize)

    sortBy.map { sort =>
      dao
        .selectAll(pageN, size, sort)
        .map(Trips)
    } match {
      case Left(e) => M.raiseError(new Exception(e))
      case Right(t) => t
    }
  }

  def select(id: Int): F[Option[Trip]] = dao.select(id)

  def insertF(trip: Trip): F[Int] =
    validateTripF(trip).flatMap(_ => dao.insert(trip))

  def updateF(id: Int, trip: Trip): F[Int] =
    validateTripF(trip).flatMap(_ => dao.update(id, trip))

  private def validateTripF(trip: Trip): F[Unit] = trip match {
    case Trip(_, _, _, _, true, None, _) => M.raiseError(new Exception("Completed trip must have non-empty distance"))
    case Trip(_, _, _, _, true, _, None) => M.raiseError(new Exception("Completed trip must have non-empty end_date"))
    case Trip(_, _, _, _, false, None, Some(_)) => M.raiseError(new Exception("Non-completed trip must have empty end_date"))
    case _ => M.pure(())
  }

  def insert(trip: Trip): Either[String, F[Int]] =
    validateTrip(trip).map(_ => dao.insert(trip))

  def update(id: Int, trip: Trip): Either[String, F[Int]] =
    validateTrip(trip).map(_ => dao.update(id, trip))

  def delete(id: Int): F[Int] = dao.delete(id)

  private def validateTrip(trip: Trip): Either[String, Unit] = trip match {
    case Trip(_, _, _, _, true, None, _) => Left("Completed trip must have non-empty distance")
    case Trip(_, _, _, _, true, _, None) => Left("Completed trip must have non-empty end_date")
    case Trip(_, _, _, _, false, None, Some(_)) => Left("Non-completed trip must have empty end_date")
    case _ => Right(())
  }
}

object TripService {
  val DefaultPage = 0
  val DefaultPageSize = 10
  val DefaultSortField: String = "id"
}
