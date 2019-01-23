package org.alexeyn.data

import java.sql.Timestamp
import java.time._

import org.alexeyn.Vehicle.Vehicle
import org.alexeyn.{Trip, Vehicle}
import slick.ast.BaseTypedType
import slick.dbio.Effect
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._
import slick.sql.FixedSqlAction

import scala.concurrent.Future

class SlickTripRepository(db: Database) extends Repository[Future] {
  implicit val vehicleEnumMapper: JdbcType[Vehicle] with BaseTypedType[Vehicle] =
    MappedColumnType.base[Vehicle, String](_.toString, Vehicle.withName)

  implicit val localDateColumnType: JdbcType[LocalDate] with BaseTypedType[LocalDate] = MappedColumnType
    .base[LocalDate, Timestamp](
      d => Timestamp.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant),
      d => d.toLocalDateTime.toLocalDate
    )

  class Trips(tag: Tag) extends Table[Trip](tag, "trips") {

    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def city = column[String]("city")
    def vehicle = column[Vehicle]("vehicle")
    def price = column[Int]("price")
    def completed = column[Boolean]("completed")
    def distance = column[Option[Int]]("distance", O.Default(None))
    def endDate = column[Option[LocalDate]]("end_date")

    def * =
      (id, city, vehicle, price, completed, distance, endDate) <>
        (Trip.tupled, Trip.unapply)
  }

  val trips = TableQuery[Trips]

  private val sorting = Map(
    "id" -> trips.sortBy(_.id),
    "city" -> trips.sortBy(_.city),
    "vehicle" -> trips.sortBy(_.vehicle),
    "price" -> trips.sortBy(_.price),
    "completed" -> trips.sortBy(_.completed),
    "distance" -> trips.sortBy(_.distance),
    "end_date" -> trips.sortBy(_.endDate)
  )

  override def createSchema(): Future[Unit] = db.run(trips.schema.create)

  override def sortingFields: Set[String] = sorting.keys.toSet

  // for testing purpose only
  def dropSchema(): FixedSqlAction[Unit, NoStream, Effect.Schema] = trips.schema.drop

  /**
   * inserts new Trip supplied Trip.id will be ignored, since id spec is auto-increment
   */
  override def insert(ca: Trip): Future[Int] = db.run(trips += ca)

  override def selectAll(page: Int = 0, pageSize: Int = 10, sort: String): Future[Seq[Trip]] = {
    sorting.get(sort) match {
      case Some(q) => db.run(q.drop(page * pageSize).take(pageSize).result)
      case None => Future.failed(new RuntimeException(s"Unknown sorting field: $sort"))
    }
  }

  override def select(id: Int): Future[Option[Trip]] =
    db.run(trips.filter(_.id === id).take(1).result.headOption)

  override def update(id: Int, row: Trip): Future[Int] = db.run(trips.filter(_.id === id).update(row))

  override def delete(id: Int): Future[Int] = db.run(trips.filter(_.id === id).delete)
}
