package org.alexeyn

import java.time.LocalDate
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import com.typesafe.config.{Config, ConfigFactory}
import org.alexeyn.RequestsSupport._
import org.alexeyn.TestData._
import org.alexeyn.json.SprayJsonCodes._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfter, DoNotDiscover}

import scala.jdk.CollectionConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

@DoNotDiscover
class E2ETest
    extends AnyWordSpec
    with Matchers
    with ScalatestRouteTest
    with BeforeAndAfter
    with ForAllTestContainer {

  override val container =  PostgreSQLContainer("postgres:10.4")

  lazy val cfg: Config = ConfigFactory.load(
    ConfigFactory
      .parseMap(
        Map(
          "port" -> container.mappedPort(5432),
          "url" -> container.jdbcUrl,
          "user" -> container.username,
          "password" -> container.password
        ).asJava
      )
      .atKey("storage")
  )

  lazy val mod = new AkkaModule(cfg)

  before {
    Await.ready(mod.db.run(mod.repo.dropSchema()), 10.seconds)
    Await.ready(mod.repo.createSchema(), 10.seconds)
  }

  "Trips service" should {
    "insert new trip" in {
      insertData()
    }

    "select trips sorted by any field" in {
      insertData()

      checkSorting("id", _.id)
      checkSorting("city", _.city)
      checkSorting("vehicle", _.vehicle.toString)
      checkSorting("price", _.price)
      checkSorting("completed", _.completed)

      // None/Null is the highest order in Postgres, so default is MaxValue then
      implicit val distanceOrdering: Ordering[Option[Int]] = Ordering.by(_.getOrElse(Int.MaxValue))
      checkSorting("distance", _.distance)

      // None/Null is the highest order in Postgres, so default is Max LocalDate then
      implicit val endDateOrdering: Ordering[Option[LocalDate]] = Ordering.by(_.getOrElse(LocalDate.MAX).toEpochDay)
      checkSorting("end_date", _.endDate)
    }

    "select trip by id" in {
      insertData()
      mockData.indices.foreach(i => selectAndCheck(i + 1))
    }

    "update trip by id" in {
      insertData()
      mockData.indices.foreach(i => updateAndCheck(i + 1))
    }

    "delete trip by id" in {
      insertData()
      deleteData()
    }

  }

  private def deleteData(): Unit =
    mockData.indices.foreach(id => deleteAndCheck(id + 1))

  private def insertData(): Unit =
    mockData.foreach { t =>
      val insert = insertRequest(t)
      insertAndCheck(insert)
    }

  private def deleteAndCheck(id: Int) = {
    val delete = deleteRequest(id)
    delete ~> mod.routes ~> check {
      commonChecks
      val result = entityAs[CommandResult]
      result.count should ===(1)
    }

    val select = selectByRequest(id)
    select ~> Route.seal(mod.routes) ~> check {
      status should ===(StatusCodes.NotFound)
    }
  }

  private def updateAndCheck(id: Int) = {
    val prefix = "updated"
    val trip = mockData(id - 1)
    val update = updateRequest(trip.copy(id, city = prefix + trip.city), id)

    update ~> mod.routes ~> check {
      commonChecks
      val result = entityAs[CommandResult]
      result.count should ===(1)
    }

    val select = selectByRequest(id)
    select ~> mod.routes ~> check {
      commonChecks
      val selected = entityAs[Trip]
      selected should ===(trip.copy(id, prefix + trip.city))
    }
  }

  private def selectAndCheck(id: Int) = {
    val select = selectByRequest(id)
    select ~> mod.routes ~> check {
      commonChecks
      val selected = entityAs[Trip]
      selected should ===(mockData(id - 1).copy(id))
    }
  }
  private def checkSorting[T](field: String, sort: Trip => T)(implicit ev: Ordering[T]) = {
    val sorted = mockData.sortBy(sort)
    val isSortedByField = (seq: Seq[Trip]) => seq.map(sort) === sorted.map(sort)
    selectAndCheck(selectAllRequest(field), sorted, isSortedByField)
  }

  private def insertAndCheck(insert: HttpRequest) = {
    insert ~> mod.routes ~> check {
      commonChecks
      val count = entityAs[CommandResult].count
      count should ===(1)
    }
  }

  private def selectAndCheck(select: HttpRequest, expected: Seq[Trip], verify: Seq[Trip] => Boolean) = {
    select ~> mod.routes ~> check {
      commonChecks
      val res = entityAs[Trips]

      res.trips.length should ===(expected.length)
      res.trips.map(_.city).toSet should ===(expected.map(_.city).toSet)
      verify(res.trips) should ===(true)
    }
  }

  private def commonChecks = {
    if (StatusCodes.OK !== status) println(s"*** Response body: $responseEntity")

    status should ===(StatusCodes.OK)
    contentType should ===(ContentTypes.`application/json`)
  }
}
