package org.alexeyn

import cats.{Functor, MonadError}
import cats.syntax.functor._
import cats.syntax.flatMap._
import org.alexeyn.TripService._
import org.alexeyn.dao.Dao

import scala.language.higherKinds

class TripService[F[_]: Functor](dao: Dao[Trip, F])(implicit M: MonadError[F, Throwable]) extends TripAlg[F] {

  override def selectAll(page: Option[Int], pageSize: Option[Int], sort: Option[String]): F[Trips] = {
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

  override def select(id: Int): F[Option[Trip]] = dao.select(id)

  override def insert(trip: Trip): F[Int] =
    validateTrip(trip).flatMap(_ => dao.insert(trip))

  override def update(id: Int, trip: Trip): F[Int] =
    validateTrip(trip).flatMap(_ => dao.update(id, trip))

  private def validateTrip(trip: Trip): F[Unit] = trip match {
    case Trip(_, _, _, _, true, None, _) => M.raiseError(new Exception("Completed trip must have non-empty distance"))
    case Trip(_, _, _, _, true, _, None) => M.raiseError(new Exception("Completed trip must have non-empty end_date"))
    case Trip(_, _, _, _, false, None, Some(_)) =>
      M.raiseError(new Exception("Non-completed trip must have empty end_date"))
    case _ => M.pure(())
  }

  override def delete(id: Int): F[Int] = dao.delete(id)
}

object TripService {
  val DefaultPage = 0
  val DefaultPageSize = 10
  val DefaultSortField: String = "id"
}
