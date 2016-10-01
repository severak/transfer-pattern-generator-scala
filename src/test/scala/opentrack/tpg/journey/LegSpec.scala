package opentrack.tpg.journey

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by linus on 30/09/16.
  */
class LegSpec extends FlatSpec with Matchers {

  def leg = Leg(
    List(
      TimetableConnection("PDW", "TBW", ConnectionType.TRAIN, 1000, 1015, "LN0001", "LN"),
      TimetableConnection("PDW", "TBW", ConnectionType.TRAIN, 1015, 1035, "LN0001", "LN" )
    )
  )

  def transferLeg = Leg(
    List (
      NonTimetableConnection("PDW", "TBW", ConnectionType.TRAIN, 15, 1015, 2359)
    )
  )

  "A leg" should "calculate it's duration" in {
    leg.duration should be (35)
    transferLeg.duration should be (15)
  }

  it should "know if it's a transfer" in {
    leg.isTransfer should be (false)
    transferLeg.isTransfer should be (true)
  }

}
