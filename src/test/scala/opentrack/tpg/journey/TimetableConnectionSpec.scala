package opentrack.tpg.journey

import org.scalatest._

class TimetableConnectionSpec extends FlatSpec with Matchers {

  def connection = TimetableConnection("PDW", "TBW", ConnectionType.TRAIN, 1000, 1015, "LN0001", "LN")

  "A timetable connection" should "return the correct duration" in {
    connection.duration should be (connection.arrivalTime - connection.departureTime)
  }

  it should "detect when a change is required" in {
    val otherConnection = TimetableConnection("PDW", "TBW", ConnectionType.TRAIN, 1000, 1015, "LN0001", "LN")
    connection.requiresInterchangeWith(otherConnection) should be (false)

    val anotherConnection = TimetableConnection("PDW", "TBW", ConnectionType.TRAIN, 1000, 1015, "LN0002", "LN")
    connection.requiresInterchangeWith(anotherConnection) should be (true)
  }
}