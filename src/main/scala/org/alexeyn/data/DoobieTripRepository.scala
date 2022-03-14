package org.alexeyn.data

import cats.effect.Sync
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.alexeyn.{Trip, Vehicle}
import org.alexeyn.Vehicle.Vehicle
import org.alexeyn.data.DoobieTripRepository._

import scala.collection.mutable

object DoobieTripRepository {
  val (columns, columnsWithComma) = {
    val columns = mutable.LinkedHashSet[String]("id", "city", "vehicle", "price", "completed", "distance", "end_date")
    (columns.toSet, columns.mkString(","))
  }

  val createStm = sql"""
         CREATE TABLE trips (
            id SERIAL,
            city VARCHAR NOT NULL,
            vehicle VARCHAR NOT NULL,
            price INT,
            completed BOOLEAN,
            distance INT,
            end_date TIMESTAMP)
       """

  val dropStm = sql"DROP TABLE IF EXISTS trips"

  val insertFrag = fr"INSERT INTO trips (" ++ Fragment.const(columnsWithComma) ++ fr")"
  val updateFrag = fr"UPDATE trips SET (" ++ Fragment.const(columnsWithComma) ++ fr") = "
}

class DoobieTripRepository[F[_]: Sync](xa: Transactor[F]) extends Repository[F] {
  implicit val vehicleMeta: Meta[Vehicle] = Meta[String].timap(s => Vehicle.withName(s))(v => v.toString)
  implicit val han: LogHandler = LogHandler.jdkLogHandler

  override def delete(id: Int): F[Int] =
    sql"DELETE FROM trips WHERE id = $id".update.run.transact(xa)

  override def update(id: Int, row: Trip): F[Int] = {
    val valuesFrag =
      fr"(${row.id}, ${row.city}, ${row.vehicle}, ${row.price}, ${row.completed}, ${row.distance}, ${row.endDate})"

    (updateFrag ++ valuesFrag).update.run.transact(xa)
  }

  override def createSchema(): F[Unit] = createStm.update.run.map(_ => ()).transact(xa)

  def dropSchema() = dropStm.update.run.map(_ => ()).transact(xa)

  override def insert(row: Trip): F[Int] = {
    val values =
      fr"VALUES (${row.id}, ${row.city}, ${row.vehicle}, ${row.price}, ${row.completed}, ${row.distance}, ${row.endDate})"

    (insertFrag ++ values).update.run.transact(xa)
  }

  override def selectAll(page: Int, pageSize: Int, sort: String): F[Seq[Trip]] =
    sql"SELECT * FROM trips"
      .query[Trip]
      .stream
      .drop(page * pageSize)
      .take(pageSize)
      .compile
      .to(Seq)
      .transact(xa)

  override def select(id: Int): F[Option[Trip]] =
    sql"SELECT * FROM trips WHERE id = $id"
      .query[Trip]
      .to[List]
      .map(_.headOption) // taking any first, this should be changed by having a validation for ID uniqueness
      .transact(xa)

  def schemaExists(): F[Unit] =
    sql"""
        SELECT 1
        FROM   information_schema.tables
        WHERE  table_catalog = 'trips'
        AND    table_name = 'trips';"""
      .query[Unit]
      .unique
      .transact(xa)

  override def sortingFields: Set[String] = DoobieTripRepository.columns
}
