package org.alexeyn

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.instances.future.catsStdInstancesForFuture
import com.softwaremill.macwire.wire
import org.alexeyn.RequestsSupport._
import org.alexeyn.TestData._
import org.alexeyn.akkahttp.QueryRoutes
import org.alexeyn.data.Repository
import org.alexeyn.json.SprayJsonCodes._
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future

class QueryRoutesTest extends WordSpec with Matchers with ScalatestRouteTest {
  val service = wire[TripService[Future]]
  val routes = wire[QueryRoutes].routes

  "QueryRoutes" should {
    "return all trips sorted by some parameter" in {
      val request = RequestsSupport.selectAllRequest("city")

      request ~> routes ~> check {
        commonChecks
        val all = entityAs[Trips].trips
        all.length should ===(3)
        all.map(_.city) should ===(Seq("berlin", "frankfurt", "munich"))
      }
    }

    "return all trips sorted by id by default" in {
      val request = selectAllRequest()

      request ~> routes ~> check {
        commonChecks
        val all = entityAs[Trips].trips
        all.length should ===(3)
        all.map(_.id) should ===(Seq(1, 2, 3))
      }
    }
  }

  private def commonChecks = {
    status should ===(StatusCodes.OK)
    contentType should ===(ContentTypes.`application/json`)
  }

  private def createStubRepo = {
    new Repository[Future] {
      override def createSchema(): Future[Unit] = Future.successful(())
      override def insert(row: Trip): Future[Int] = Future.successful(1)
      override def selectAll(page: Int, pageSize: Int, sort: String): Future[Seq[Trip]] = {
        sort match {
          case "id" => Future.successful(mockData.sortBy(_.id))
          case "city" => Future.successful(mockData.sortBy(_.city))
        }
      }
      override def select(id: Int): Future[Option[Trip]] = Future.successful(mockData.lift(id))
      override def delete(id: Int): Future[Int] = ???
      override def update(id: Int, row: Trip): Future[Int] = ???
      override def sortingFields: Set[String] = Set("id", "city")
    }
  }
}
