package org.alexeyn

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import cats.instances.future.catsStdInstancesForFuture
import com.softwaremill.macwire.wire
import org.alexeyn.TestData.tripId
import org.alexeyn.http.CommandRoutes
import org.alexeyn.json.GenericJsonWriter
import org.scalameter._
import org.scalatest.{DoNotDiscover, FlatSpec, Matchers}

import scala.concurrent.Future

@DoNotDiscover
class JsonPerfTest extends FlatSpec with ScalatestRouteTest with Matchers {

  val standardConfig =
    config(
      Key.exec.minWarmupRuns -> 500,
      Key.exec.maxWarmupRuns -> 1000,
      Key.exec.benchRuns -> 10000,
      Key.verbose -> false
    ).withWarmer(new Warmer.Default)

  val service = wire[TripService[Future]]

  it should "print performance statistics for Upickle Json library" in {
    import org.alexeyn.json.UpickleJsonCodes._
    val routes: Route = wire[CommandRoutes].routes

    val time = {
      standardConfig.measure {
        insertTrip(routes)
        updateTrip(routes)
      }
    }

    println(s"upickle-ujson time: $time")
  }

  it should "print performance statistics for Spray Json library" in {
    import org.alexeyn.json.SprayJsonCodes._
    val routes: Route = wire[CommandRoutes].routes

    val time = {
      standardConfig.measure {
        insertTrip(routes)
        updateTrip(routes)
      }
    }

    println(s"spray-json time: $time")

  }

  private def insertTrip(
    routes: Route
  )(implicit w: GenericJsonWriter[Trip], ev: FromEntityUnmarshaller[CommandResult]) = {
    TestData.mockData.foreach { t =>
      val request = RequestsSupport.insertRequest(t)
      request ~> routes ~> check {
        val count = entityAs[CommandResult].count
        count should ===(1)
      }
    }
  }

  private def updateTrip(
    routes: Route
  )(implicit w: GenericJsonWriter[Trip], ev: FromEntityUnmarshaller[CommandResult]) = {
    TestData.mockData.foreach { t =>
      val request = RequestsSupport.updateRequest(t, t.id)
      request ~> routes ~> check {
        val count = entityAs[CommandResult].count
        count should ===(1)
      }
    }
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
