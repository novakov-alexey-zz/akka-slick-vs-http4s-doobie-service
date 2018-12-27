package org.alexeyn

import cats.Functor
import cats.syntax.functor._
import org.alexeyn.TripService._

import scala.language.higherKinds

class TripService[F[_]](dao: Dao[Trip, F])(implicit F: Functor[F]) {

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

  def select(id: Int): F[Option[Trip]] = dao.select(id)

  def insert(trip: Trip): Either[String, F[Int]] =
    validateTrip(trip).map(_ => dao.insert(trip))

  def update(id: Int, trip: Trip): Either[String, F[Int]] =
    validateTrip(trip).map(_ => dao.update(id, trip))

  def delete(id: Int): F[Int] = dao.delete(id)

  private def validateTrip(trip: Trip): Either[String, Unit] = trip match {
    case Trip(_, _, _, _, true, None, _) => Left("Completed trip must have non-empty distance")
    case Trip(_, _, _, _, true, _, None) => Left("Completed trip must have non-empty end_date")
    case Trip(_, _, _, _, false, None, Some(_)) => Left("Non-completed trip must have empty end_date")
    case _ => Right()
  }
}

object TripService {
  val DefaultPage = 0
  val DefaultPageSize = 10
  val DefaultSortField: String = "id"
}
