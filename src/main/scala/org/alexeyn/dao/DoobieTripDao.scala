package org.alexeyn.dao

import cats.effect.Sync
import doobie._
import doobie.implicits._
import org.alexeyn.{Trip, Vehicle}
import org.alexeyn.Vehicle.Vehicle
import org.alexeyn.dao.DoobieTripDao._

import scala.collection.mutable

object DoobieTripDao {
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

class DoobieTripDao[F[_]](xa: Transactor[F])(implicit F: Sync[F]) extends Dao[Trip, F] {
  implicit val vehicleMeta: Meta[Vehicle] = Meta[String].timap(s => Vehicle.withName(s))(v => v.toString)
  implicit val han: LogHandler = LogHandler.jdkLogHandler

  override def delete(id: Int): F[Int] =
    sql"DELETE FROM trips WHERE id = $id".update.run.transact(xa)

  override def update(id: Int, row: Trip) = {
    val valuesFrag =
      fr"(${row.id}, ${row.city}, ${row.vehicle}, ${row.price}, ${row.completed}, ${row.distance}, ${row.endDate})"

    (updateFrag ++ valuesFrag).update.run.transact(xa)
  }

  override def createSchema() = createStm.update.run.map(_ => ()).transact(xa)

  def dropSchema() = dropStm.update.run.map(_ => ()).transact(xa)

  override def insert(row: Trip) = {
    val values =
      fr"VALUES (${row.id}, ${row.city}, ${row.vehicle}, ${row.price}, ${row.completed}, ${row.distance}, ${row.endDate})"

    (insertFrag ++ values).update.run.transact(xa)
  }

  override def selectAll(page: Int, pageSize: Int, sort: String) =
    sql"select * from trips"
      .query[Trip]
      .stream
      .drop(page * pageSize)
      .take(pageSize)
      .compile
      .to[Seq]
      .transact(xa)

  override def select(id: Int) =
    sql"SELECT * FROM trips"
      .query[Trip]
      .option
      .transact(xa)

  def testDB() = sql"""
        SELECT 1
        FROM   information_schema.tables
        WHERE  table_catalog = 'trips'
        AND    table_name = 'trips';"""
    .query[Int].unique.transact(xa)

  override def sortingFields = DoobieTripDao.columns
}
