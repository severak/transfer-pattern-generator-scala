package opentrack.tpg.journey

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by linus on 30/09/16.
  */
class JourneySpec extends FlatSpec with Matchers {

  def journey = Journey(
    Leg(
      TimetableConnection("ORP", "LBG", ConnectionType.TRAIN, 1000, 1015, "LN0001", "LN") ::
      TimetableConnection("LBG", "WAE", ConnectionType.TRAIN, 1015, 1035, "LN0001", "LN") :: Nil
    ) ::
    Leg(
      NonTimetableConnection("WAE", "CHX", ConnectionType.TRAIN, 15, 1015, 2359) :: Nil
    ) :: Nil
  )

  def journey2 = Journey(
    Leg(
      NonTimetableConnection("SEV", "ORP", ConnectionType.TRAIN, 15, 915, 2359) :: Nil
    ) ::
    Leg(
      TimetableConnection("ORP", "LBG", ConnectionType.TRAIN, 1030, 1045, "LN0001", "LN") ::
      TimetableConnection("LBG", "WAE", ConnectionType.TRAIN, 1045, 1055, "LN0001", "LN") :: Nil
    ) :: Nil
  )

  "A journey" should "calculate it's hash" in {
    journey.hash should be ("ORPWAE")
  }

  it should "calculate it's departure time" in {
    journey.departureTime should be (1000)
    journey2.departureTime should be (1015)
  }

  it should "calculate it's arrival time" in {
    journey.arrivalTime should be (1050)
    journey2.arrivalTime should be (1055)
  }

}
