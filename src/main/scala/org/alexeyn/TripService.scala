package org.alexeyn

import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.alexeyn.TripService._
import org.alexeyn.data.Repository

import scala.language.higherKinds

class TripService[F[_]](repo: Repository[F])(implicit M: MonadError[F, Throwable]) extends TripAlg[F] {

  override def selectAll(page: Option[Int], pageSize: Option[Int], sort: Option[String]): F[Trips] = {
    val sortBy = sort
      .map(s => repo.sortingFields.find(_ == s).toRight(UnknownSortField(s)))
      .getOrElse(Right(DefaultSortField))

    M.fromEither(sortBy).flatMap { sort =>
      val pageN = page.getOrElse(DefaultPage)
      val size = pageSize.getOrElse(DefaultPageSize)

      repo
        .selectAll(pageN, size, sort)
        .map(Trips)
    }
  }

  override def select(id: Int): F[Option[Trip]] = repo.select(id)

  override def insert(trip: Trip): F[Int] =
    validateTrip(trip).flatMap(_ => repo.insert(trip))

  override def update(id: Int, trip: Trip): F[Int] =
    validateTrip(trip).flatMap(_ => repo.update(id, trip))

  private val validateTrip: Trip => F[Unit] = {
    case t @ Trip(_, _, _, _, true, None, _) =>
      M.raiseError(InvalidTrip(t, "completed trip must have non-empty 'distance'"))
    case t @ Trip(_, _, _, _, true, _, None) =>
      M.raiseError(InvalidTrip(t, "completed trip must have non-empty 'end_date'"))
    case t @ Trip(_, _, _, _, false, None, Some(_)) =>
      M.raiseError(InvalidTrip(t, "non-completed trip must have empty 'end_date'"))
    case _ => M.pure(())
  }

  override def delete(id: Int): F[Int] = repo.delete(id)
}

object TripService {
  val DefaultPage = 0
  val DefaultPageSize = 10
  val DefaultSortField: String = "id"
}
