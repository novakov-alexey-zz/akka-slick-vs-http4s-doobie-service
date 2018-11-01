package org.alexeyn

trait Dao[T, U[_]] {
  def delete(id: Int) : U[Int]

  def update(id: Int, row: T): U[Int]

  def createSchema(): U[Unit]

  def insert(row: T): U[Int]

  def selectAll(page: Int, pageSize: Int, sort: String): U[Seq[T]]

  def select(id: Int): U[Option[T]]

  def sortingFields: Set[String]
}
