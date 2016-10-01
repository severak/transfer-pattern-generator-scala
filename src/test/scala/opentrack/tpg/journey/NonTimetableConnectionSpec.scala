package opentrack.tpg.journey

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by linus on 30/09/16.
  */
class NonTimetableConnectionSpec extends FlatSpec with Matchers {

  def connection = NonTimetableConnection("PDW", "TBW", ConnectionType.TRAIN, 15, 1015, 2359)

  "A non-timetable connection" should "detect when a change is required" in {
    val otherConnection = TimetableConnection("PDW", "TBW", ConnectionType.TRAIN, 1000, 1015, "LN0001", "LN" )
    connection.requiresInterchangeWith(otherConnection) should be (true)
  }
}