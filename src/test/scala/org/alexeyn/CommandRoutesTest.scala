package org.alexeyn

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.instances.future.catsStdInstancesForFuture
import com.softwaremill.macwire.wire
import org.alexeyn.TestData._
import org.alexeyn.akkahttp.CommandRoutes
import org.alexeyn.data.Repository
import org.alexeyn.json.SprayJsonCodes._
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future

class CommandRoutesTest extends WordSpec with Matchers with ScalatestRouteTest {

  val service = wire[TripService[Future]]
  val routes = wire[CommandRoutes].routes

  "CommandRoutes" should {
    "insert new trip and return its id" in {
      val request = RequestsSupport.insertRequest(berlin)

      request ~> routes ~> check {
        commonChecks
        val count = entityAs[CommandResult].count
        count should ===(1)
      }
    }

    "update existing trip and return count" in {
      val request = RequestsSupport.updateRequest(berlin, tripId)

      request ~> routes ~> check {
        commonChecks
        val count = entityAs[CommandResult].count
        count should ===(1)
      }
    }

    "delete existing trip and return count" in {
      val request = RequestsSupport.deleteRequest(tripId)

      request ~> routes ~> check {
        commonChecks
        val count = entityAs[CommandResult].count
        count should ===(1)
      }
    }

    "reject new trip creation when endDate is empty for a completed trip" in {
      val request = RequestsSupport.insertRequest(berlin.copy(completed = true, endDate = None))

      request ~> routes ~> check {
        status should ===(StatusCodes.PreconditionFailed)
      }
    }

    "reject new trip creation when distance is empty for a completed trip" in {
      val request = RequestsSupport.insertRequest(berlin.copy(completed = true, distance = None))

      request ~> routes ~> check {
        status should ===(StatusCodes.PreconditionFailed)
      }
    }

    "reject existing trip modification when distance is empty for a completed trip" in {
      val request = RequestsSupport.updateRequest(berlin.copy(completed = true, distance = None), tripId)

      request ~> routes ~> check {
        status should ===(StatusCodes.PreconditionFailed)
      }
    }

    "reject existing trip modification when endDate is empty for a completed trip" in {
      val request = RequestsSupport.updateRequest(berlin.copy(completed = true, endDate = None), tripId)

      request ~> routes ~> check {
        status should ===(StatusCodes.PreconditionFailed)
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
      override def insert(row: Trip): Future[Int] = Future.successful(tripId)
      override def selectAll(page: Int, pageSize: Int, sort: String): Future[Seq[Trip]] = ???
      override def select(id: Int): Future[Option[Trip]] = ???
      override def delete(id: Int): Future[Int] = Future.successful(tripId)
      override def update(id: Int, row: Trip): Future[Int] = Future.successful(tripId)
      override def sortingFields: Set[String] = ???
    }
  }
}
