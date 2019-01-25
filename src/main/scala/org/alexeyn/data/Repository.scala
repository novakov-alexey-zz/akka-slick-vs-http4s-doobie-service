package org.alexeyn.data

import org.alexeyn.Trip

trait Repository[F[_]] {
  def delete(id: Int): F[Int]

  def update(id: Int, row: Trip): F[Int]

  def createSchema(): F[Unit]

  def insert(row: Trip): F[Int]

  def selectAll(page: Int, pageSize: Int, sort: String): F[Seq[Trip]]

  def select(id: Int): F[Option[Trip]]

  def sortingFields: Set[String]
}
