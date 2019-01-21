package org.alexeyn

trait TripAlg[F[_]] {

  def selectAll(page: Option[Int], pageSize: Option[Int], sort: Option[String]): F[Trips]
  def select(id: Int): F[Option[Trip]]
  def insert(trip: Trip): F[Int]
  def update(id: Int, trip: Trip): F[Int]
  def delete(id: Int): F[Int]
}
