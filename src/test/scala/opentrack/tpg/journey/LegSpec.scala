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

  it should "be able to merge to an earlier leg" in {
    val service = Leg(
      List(
        TimetableConnection("PDW", "TON", ConnectionType.TRAIN, 1000, 1015, "LN0001", "LN"),
        TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1015, 1035, "LN0001", "LN"),
        TimetableConnection("SEV", "ORP", ConnectionType.TRAIN, 1035, 1045, "LN0001", "LN"),
        TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1045, 1055, "LN0001", "LN")
      )
    )

    val pointlessLeg = Leg(
      List(
        TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1015, 1035, "LN0001", "LN")
      )
    )

    val mergedLeg = Leg(
      List(
        TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1015, 1035, "LN0001", "LN"),
        TimetableConnection("SEV", "ORP", ConnectionType.TRAIN, 1035, 1045, "LN0001", "LN")
      )
    )

    service.getReplacement(pointlessLeg, "ORP") should be (mergedLeg)
  }

  it should "be able to merge to an earlier leg where the service terminates" in {
    val service = Leg(
      List(
        TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1015, 1035, "LN0001", "LN"),
        TimetableConnection("SEV", "ORP", ConnectionType.TRAIN, 1035, 1045, "LN0001", "LN")
      )
    )

    val pointlessLeg = Leg(
      List(
        TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1015, 1035, "LN0001", "LN")
      )
    )

    val mergedLeg = Leg(
      List(
        TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1015, 1035, "LN0001", "LN"),
        TimetableConnection("SEV", "ORP", ConnectionType.TRAIN, 1035, 1045, "LN0001", "LN")
      )
    )

    service.getReplacement(pointlessLeg, "ORP") should be (mergedLeg)
  }

  it should "check the origin of the new service is reachable" in {
    val service = Leg(
      List(
        TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1015, 1035, "LN0001", "LN"),
        TimetableConnection("SEV", "ORP", ConnectionType.TRAIN, 1035, 1045, "LN0001", "LN")
      )
    )

    val expected = Leg(
      List(
        TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1017, 1035, "LN0002", "LN")
      )
    )

    service.getReplacement(expected, "ORP") should be (expected)
  }


}
