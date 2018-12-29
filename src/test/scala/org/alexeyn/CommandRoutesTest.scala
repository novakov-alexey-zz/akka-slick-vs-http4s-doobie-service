package org.alexeyn

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.instances.future._
import org.alexeyn.TestData._
import org.alexeyn.http.CommandRoutes
import org.alexeyn.json.SprayJsonCodes._
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future

class CommandRoutesTest extends WordSpec with Matchers with ScalatestRouteTest {

  private val stubDao = createStubDao
  private val service = new TripService[Future](stubDao)
  private val routes = CommandRoutes.routes(service)

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

  private def createStubDao = {
    new Dao[Trip, Future] {
      override def createSchema(): Future[Unit] = Future.successful()
      override def insert(row: Trip): Future[Int] = Future.successful(tripId)
      override def selectAll(page: Int, pageSize: Int, sort: String): Future[Seq[Trip]] = ???
      override def select(id: Int): Future[Option[Trip]] = ???
      override def delete(id: Int): Future[Int] = Future.successful(tripId)
      override def update(id: Int, row: Trip): Future[Int] = Future.successful(tripId)
      override def sortingFields: Set[String] = ???
    }
  }
}
