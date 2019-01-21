package org.alexeyn.dao

import cats.effect.IO
import org.alexeyn.Trip

import scala.collection.mutable

class DoobieTripDao extends Dao[Trip, IO] {
  var state = mutable.Map[Int, Trip]()

  override def delete(id: Int) = {
    state -= id
    IO.pure(id)
  }
  override def update(id: Int, row: Trip) = {
    state += (id -> row)
    IO.pure(id)
  }
  override def createSchema() = IO.unit
  override def insert(row: Trip) = {
    state += (row.id -> row)
    IO(state.size)
  }
  override def selectAll(page: Int, pageSize: Int, sort: String) = IO(state.values.toSeq)
  override def select(id: Int) = IO(state.get(id))
  override def sortingFields = Set("id")
}
