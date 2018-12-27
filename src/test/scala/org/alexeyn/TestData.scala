package org.alexeyn

import java.time.LocalDate

object TestData {
  val adId = 1
  val berlin = Trip(adId, "berlin", Vehicle.Car, 20000, completed = false, Some(20000), Some(LocalDate.of(2010, 4, 22)))

  val mockData: IndexedSeq[Trip] =
    IndexedSeq(
      Trip(2, "frankfurt", Vehicle.Taxi, 2000, completed = false, Some(20000), Some(LocalDate.of(2000, 4, 22))),
      berlin,
      Trip(3, "munich", Vehicle.Bike, 2000, completed = false, None, None)
    )
}
