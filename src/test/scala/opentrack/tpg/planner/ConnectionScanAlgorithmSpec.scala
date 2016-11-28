package opentrack.tpg.planner

import opentrack.tpg.journey._
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.immutable.HashMap
import scala.collection.mutable

/**
  * Created by linus on 30/09/16.
  */
class ConnectionScanAlgorithmSpec extends FlatSpec with Matchers {

  "Connection scan algorithm" should "plan a basic journey" in {
    val timetable = List(
      TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1234", "CS"),
      TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS1234", "CS"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1100, 1115, "CS1234", "CS")
    )

    val scanner = new ConnectionScanAlgorithm(timetable, HashMap(), HashMap())
    val actual = scanner.getJourney("A", "D", 900)
    val expected = Some(Journey(List(Leg(timetable))))

    actual should be(expected)
  }

  it should "plan a journey with early termination" in {
    val timetable = List(
      TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1234", "CS"),
      TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS1234", "CS"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1100, 1115, "CS1234", "CS"),
      TimetableConnection("D", "E", ConnectionType.TRAIN, 1115, 1120, "CS1234", "CS")
    )

    val scanner = new ConnectionScanAlgorithm(timetable, HashMap(), HashMap())
    val actual = scanner.getJourney("A", "D", 900)
    val expected = Journey(List(
      Leg(List(
        TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1234", "CS"),
        TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS1234", "CS"),
        TimetableConnection("C", "D", ConnectionType.TRAIN, 1100, 1115, "CS1234", "CS")
      ))
    ))

    actual should be(Some(expected))
  }

  it should "plan a journey with multiple routes" in {
    val timetable = List(
      TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1234", "CS"),
      TimetableConnection("A", "C", ConnectionType.TRAIN, 1005, 1025, "CS1234", "CS"),
      TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS1234", "CS"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1030, 1100, "CS1234", "CS"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1100, 1115, "CS1234", "CS"),
      TimetableConnection("D", "E", ConnectionType.TRAIN, 1105, 1125, "CS1234", "CS"),
      TimetableConnection("D", "E", ConnectionType.TRAIN, 1120, 1135, "CS1234", "CS")
    )

    val scanner = new ConnectionScanAlgorithm(timetable, HashMap(), HashMap())
    val actual = scanner.getJourney("A", "E", 900)
    val expected = Journey(List(
      Leg(List(
        TimetableConnection("A", "C", ConnectionType.TRAIN, 1005, 1025, "CS1234", "CS"),
        TimetableConnection("C", "D", ConnectionType.TRAIN, 1030, 1100, "CS1234", "CS"),
        TimetableConnection("D", "E", ConnectionType.TRAIN, 1105, 1125, "CS1234", "CS")
      ))
    ))

    actual should be(Some(expected))
  }

  it should "fail to plan a journey with no route" in {
    val timetable = List(
      TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1234", "CS"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1100, 1115, "CS1234", "CS"),
      TimetableConnection("D", "E", ConnectionType.TRAIN, 1105, 1125, "CS1234", "CS"),
      TimetableConnection("D", "E", ConnectionType.TRAIN, 1120, 1135, "CS1234", "CS")
    )

    val scanner = new ConnectionScanAlgorithm(timetable, HashMap(), HashMap())
    val actual = scanner.getJourney("A", "E", 900)
    val expected = None

    actual should be(expected)
  }


  it should "fail to plan a journey with no route because of a missed connection" in {
    val timetable = List(
      TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1234", "LN"),
      TimetableConnection("B", "C", ConnectionType.TRAIN, 1001, 1045, "CS1234", "LN"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1100, 1115, "CS1234", "LN"),
      TimetableConnection("D", "E", ConnectionType.TRAIN, 1105, 1125, "CS1234", "LN"),
      TimetableConnection("D", "E", ConnectionType.TRAIN, 1120, 1135, "CS1234", "LN")
    )

    val scanner = new ConnectionScanAlgorithm(timetable, HashMap(), HashMap())
    val actual = scanner.getJourney("A", "E", 900)
    val expected = None

    actual should be(expected)
  }

  it should "plan a journey with using non timetable connections" in {
    val timetable = List(
      TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1234", "LN"),
      TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS1234", "LN"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1030, 1100, "CS1234", "LN"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1100, 1115, "CS1234", "LN")
    )

    val nonTimetable = HashMap(
      "B" -> List(
        NonTimetableConnection("B", "C", ConnectionType.TUBE, 5, 0, 2359),
        NonTimetableConnection("B", "E", ConnectionType.TUBE, 5, 0, 2359)
      )
    )

    val scanner = new ConnectionScanAlgorithm(timetable, nonTimetable, HashMap())
    val actual = scanner.getJourney("A", "D", 900)
    val expected = Journey(List(
      Leg(List(TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1234", "LN"))),
      Leg(List(NonTimetableConnection("B", "C", ConnectionType.TUBE, 5, 0, 2359))),
      Leg(List(TimetableConnection("C", "D", ConnectionType.TRAIN, 1030, 1100, "CS1234", "LN")))
    ))

    actual should be(Some(expected))
  }

  it should "plan a journey avoiding non timetable connections that can't be used" in {
    val timetable = List(
      TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1234", "LN"),
      TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS1234", "LN"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1030, 1100, "CS1234", "LN"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1100, 1115, "CS1234", "LN")
    )

    val nonTimetable = HashMap(
      "B" -> List(
        NonTimetableConnection("B", "C", ConnectionType.TUBE, 5, 100, 200),
        NonTimetableConnection("B", "E", ConnectionType.TUBE, 5, 0, 2359)
      )
    )

    val scanner = new ConnectionScanAlgorithm(timetable, nonTimetable, HashMap())
    val actual = scanner.getJourney("A", "D", 900)
    val expected = Journey(List(
      Leg(List(
        TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1234", "LN"),
        TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS1234", "LN"),
        TimetableConnection("C", "D", ConnectionType.TRAIN, 1100, 1115, "CS1234", "LN")
      ))
    ))

    actual should be(Some(expected))
  }

  it should "plan a journey avoiding non timetable connections that aren't better" in {
    val timetable = List(
      TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1234", "LN"),
      TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS1234", "LN"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1030, 1100, "CS1234", "LN"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1100, 1115, "CS1234", "LN")
    )

    val nonTimetable = HashMap(
      "B" -> List(
        NonTimetableConnection("B", "C", ConnectionType.TUBE, 500, 0, 2359),
        NonTimetableConnection("B", "E", ConnectionType.TUBE, 5, 0, 2359)
      )
    )

    val scanner = new ConnectionScanAlgorithm(timetable, nonTimetable, HashMap())
    val actual = scanner.getJourney("A", "D", 900)
    val expected = Journey(List(
      Leg(List(
        TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1234", "LN"),
        TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS1234", "LN"),
        TimetableConnection("C", "D", ConnectionType.TRAIN, 1100, 1115, "CS1234", "LN")
      ))
    ))

    actual should be(Some(expected))
  }

  it should "plan a journey starting with a non timetable connection" in {
    val timetable = List(
      TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1234", "LN"),
      TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS1234", "LN"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1030, 1100, "CS1234", "LN"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1100, 1115, "CS1234", "LN")
    )

    val nonTimetable = HashMap(
      "A" -> List(
        NonTimetableConnection("A", "B", ConnectionType.TUBE, 5, 0, 2359)
      )
    )

    val scanner = new ConnectionScanAlgorithm(timetable, nonTimetable, HashMap())
    val actual = scanner.getJourney("A", "D", 900)
    val expected = Journey(List(
      Leg(List(NonTimetableConnection("A", "B", ConnectionType.TUBE, 5, 0, 2359))),
      Leg(List(TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS1234", "LN"),
        TimetableConnection("C", "D", ConnectionType.TRAIN, 1100, 1115, "CS1234", "LN")))
    ))
    actual should be(Some(expected))
  }

  it should "plan a journey starting only using a non timetable connection" in {
    val timetable = List(
      TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1234", "LN"),
      TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS1234", "LN"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1030, 1100, "CS1234", "LN"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1100, 1115, "CS1234", "LN")
    )

    val nonTimetable = HashMap(
      "A" -> List(
        NonTimetableConnection("A", "D", ConnectionType.TUBE, 5, 0, 2359)
      )
    )

    val scanner = new ConnectionScanAlgorithm(timetable, nonTimetable, HashMap())
    val actual = scanner.getJourney("A", "D", 900)
    val expected = Journey(List(
      Leg(List(NonTimetableConnection("A", "D", ConnectionType.TUBE, 5, 0, 2359)))
    ))
    actual should be(Some(expected))
  }

  it should "plan a journey with a change of service" in {
    val timetable = List(
      TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1000", "LN"),
      TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS2000", "LN"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1045, 1115, "CS2000", "LN")
    )

    val interchange = HashMap(
      "A" -> 5,
      "B" -> 5,
      "C" -> 5
    )

    val scanner = new ConnectionScanAlgorithm(timetable, HashMap(), interchange)
    val actual = scanner.getJourney("A", "D", 900)
    val expected = Journey(List(
      Leg(List(TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1000", "LN"))),
      Leg(List(
        TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS2000", "LN"),
        TimetableConnection("C", "D", ConnectionType.TRAIN, 1045, 1115, "CS2000", "LN")
      ))
    ))
    actual should be(Some(expected))
  }

  it should "fail to plan a journey because of interchange time" in {
    val timetable = List(
      TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1000", "LN"),
      TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS2000", "LN"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1045, 1115, "CS2000", "LN")
    )

    val interchange = HashMap(
      "A" -> 6,
      "B" -> 6,
      "C" -> 6
    )

    val scanner = new ConnectionScanAlgorithm(timetable, HashMap(), interchange)
    val actual = scanner.getJourney("A", "D", 900)
    val expected = None
    actual should be(expected)
  }

  it should "plan a journey where walking if faster than change of service" in {
    val timetable = List(
      TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1000, 1040, "SE1000", "LN"),
      TimetableConnection("WAE", "CHX", ConnectionType.TRAIN, 1040, 1045, "SE1000", "LN"),
      TimetableConnection("CHX", "LBG", ConnectionType.TRAIN, 1050, 1055, "SE2000", "LN"),
      TimetableConnection("CHX", "LBG", ConnectionType.TRAIN, 1105, 1110, "SE2000", "LN")
    )

    val nonTimetable = HashMap(
      "WAE" -> List(
        NonTimetableConnection("WAE", "LBG", ConnectionType.TUBE, 20, 0, 2359)
      )
    )

    val interchange = HashMap(
      "CHX" -> 10
    )

    val scanner = new ConnectionScanAlgorithm(timetable, nonTimetable, interchange)
    val actual = scanner.getJourney("ORP", "LBG", 900)
    val expected = Journey(List(
      Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1000, 1040, "SE1000", "LN"))),
      Leg(List(NonTimetableConnection("WAE", "LBG", ConnectionType.TUBE, 20, 0, 2359)))
    ))
    actual should be(Some(expected))
  }


  it should "plan a journey where a change is faster than walking" in {
    val timetable = List(
      TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1000, 1040, "SE1000", "LN"),
      TimetableConnection("WAE", "CHX", ConnectionType.TRAIN, 1040, 1045, "SE1000", "LN"),
      TimetableConnection("CHX", "LBG", ConnectionType.TRAIN, 1050, 1055, "SE2000", "LN")
    )

    val nonTimetable = HashMap(
      "WAE" -> List(
        NonTimetableConnection("WAE", "LBG", ConnectionType.TUBE, 20, 0, 2359)
      )
    )

    val interchange = HashMap(
      "CHX" -> 5
    )

    val scanner = new ConnectionScanAlgorithm(timetable, nonTimetable, interchange)
    val actual = scanner.getJourney("ORP", "LBG", 900)
    val expected = Journey(List(
      Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1000, 1040, "SE1000", "LN"),
        TimetableConnection("WAE", "CHX", ConnectionType.TRAIN, 1040, 1045, "SE1000", "LN"))),
      Leg(List(TimetableConnection("CHX", "LBG", ConnectionType.TRAIN, 1050, 1055, "SE2000", "LN")))
    ))
    actual should be(Some(expected))
  }

  it should "plan a journey with multiple change points" in {
    val timetable = List(
      TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1000", "LN"),
      TimetableConnection("B", "C", ConnectionType.TRAIN, 1016, 1020, "CS1000", "LN"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1021, 1025, "CS1000", "LN"),
      TimetableConnection("D", "F", ConnectionType.TRAIN, 1026, 1030, "CS1000", "LN"),
      TimetableConnection("0", "B", ConnectionType.TRAIN, 1005, 1027, "CS2000", "LN"),
      TimetableConnection("B", "C", ConnectionType.TRAIN, 1028, 1032, "CS2000", "LN"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1033, 1037, "CS2000", "LN"),
      TimetableConnection("D", "E", ConnectionType.TRAIN, 1038, 1042, "CS2000", "LN")
    )

    val interchange = HashMap(
      "A" -> 5,
      "B" -> 5,
      "C" -> 5,
      "D" -> 5
    )

    val scanner = new ConnectionScanAlgorithm(timetable, HashMap(), interchange)
    val actual = scanner.getJourney("A", "E", 900)
    val expected = Journey(List(
      Leg(List(
        TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1015, "CS1000", "LN"),
        TimetableConnection("B", "C", ConnectionType.TRAIN, 1016, 1020, "CS1000", "LN"),
        TimetableConnection("C", "D", ConnectionType.TRAIN, 1021, 1025, "CS1000", "LN")
      )),
      Leg(List(
        TimetableConnection("D", "E", ConnectionType.TRAIN, 1038, 1042, "CS2000", "LN")
      ))
    ))
    actual should be(Some(expected))
  }

  it should "shortest path tree" in {
    val timetable = List(
      TimetableConnection("SEV", "ORP", ConnectionType.TRAIN, 900, 940, "SE1000", "LN"),
      TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1000, 1040, "SE1000", "LN")
    )

    val scanner = new ConnectionScanAlgorithm(timetable, HashMap(), HashMap())
    val actual = scanner.getShortestPathTree("SEV")
    val expected = mutable.HashMap(
      "ORP" -> mutable.HashMap(
        940-> Journey(List(
          Leg(List(
            TimetableConnection("SEV", "ORP", ConnectionType.TRAIN, 900, 940, "SE1000", "LN")
          ))
        ))
      ),
      "WAE" -> mutable.HashMap(
        1040-> Journey(List(
          Leg(List(
            TimetableConnection("SEV", "ORP", ConnectionType.TRAIN, 900, 940, "SE1000", "LN"),
            TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1000, 1040, "SE1000", "LN")
          ))
        ))
      )
    )

    actual should be (expected)
  }


  it should "shortest path tree with change" in {
    val timetable = List(
      TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1000, 1040, "SE1000", "LN"),
      TimetableConnection("WAE", "CHX", ConnectionType.TRAIN, 1040, 1045, "SE1000", "LN"),
      TimetableConnection("CHX", "LBG", ConnectionType.TRAIN, 1050, 1055, "SE2000", "LN"),
      TimetableConnection("CHX", "LBG", ConnectionType.TRAIN, 1052, 1053, "SE2500", "LN"),
      TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1100, 1140, "SE3000", "LN")
    )

    val nonTimetable = HashMap(
      "WAE" -> List(
        NonTimetableConnection("WAE", "LBG", ConnectionType.TUBE, 19, 0, 2359)
      )
    )

    val interchange = HashMap(
      "CHX" -> 5
    )

    val scanner = new ConnectionScanAlgorithm(timetable, nonTimetable, interchange)
    val actual = scanner.getShortestPathTree("ORP")
    val expected = mutable.HashMap(
      "WAE" -> mutable.HashMap(
        1040 -> Journey(List(
          Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1000, 1040, "SE1000", "LN")))
        )),
        1140 -> Journey(List(
          Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1100, 1140, "SE3000", "LN")))
        ))
      ),
      "LBG" -> mutable.HashMap(
        1053 -> Journey(List(
          Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1000, 1040, "SE1000", "LN"),
            TimetableConnection("WAE", "CHX", ConnectionType.TRAIN, 1040, 1045, "SE1000", "LN"))),
          Leg(List(TimetableConnection("CHX", "LBG", ConnectionType.TRAIN, 1052, 1053, "SE2500", "LN")))
        )),
        1159 -> Journey(List(
          Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1100, 1140, "SE3000", "LN"))),
          Leg(List(NonTimetableConnection("WAE", "LBG", ConnectionType.TUBE, 19, 0, 2359)))
        ))
      ),
      "CHX" -> mutable.HashMap(
        1045 -> Journey(List(
          Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1000, 1040, "SE1000", "LN"),
            TimetableConnection("WAE", "CHX", ConnectionType.TRAIN, 1040, 1045, "SE1000", "LN")))
        ))
      )
    )

    actual should be(expected)
  }


  it should "transfer patterns with change" in {
    val timetable = List(
      TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1000, 1040, "SE1000", "LN"),
      TimetableConnection("WAE", "CHX", ConnectionType.TRAIN, 1040, 1045, "SE1000", "LN"),
      TimetableConnection("CHX", "LBG", ConnectionType.TRAIN, 1050, 1055, "SE2000", "LN"),
      TimetableConnection("CHX", "LBG", ConnectionType.TRAIN, 1052, 1053, "SE2500", "LN"),
      TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1100, 1140, "SE3000", "LN")
    )

    val nonTimetable = HashMap(
      "WAE" -> List(
        NonTimetableConnection("WAE", "LBG", ConnectionType.TUBE, 5, 0, 2359)
      )
    )

    val interchange = HashMap(
      "CHX" -> 5
    )

    val scanner = new ConnectionScanAlgorithm(timetable, nonTimetable, interchange)
    val actual = scanner.getShortestPathTree("ORP")
    val expected = mutable.HashMap(
      "WAE" -> mutable.HashMap(
        1040-> Journey(List(
          Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1000, 1040, "SE1000", "LN") ))
        )),
        1140-> Journey(List(
          Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1100, 1140, "SE3000", "LN") ))
        ))
      ),
      "LBG" -> mutable.HashMap(
        1045-> Journey(List(
          Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1000, 1040, "SE1000", "LN"))),
          Leg(List(NonTimetableConnection("WAE", "LBG", ConnectionType.TUBE, 5, 0, 2359)))
        )),
        1145-> Journey(List(
          Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1100, 1140, "SE3000", "LN"))),
          Leg(List(NonTimetableConnection("WAE", "LBG", ConnectionType.TUBE, 5, 0, 2359)))
        ))
      ),
      "CHX" -> mutable.HashMap(
        1045-> Journey(List(
          Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1000, 1040, "SE1000", "LN"),
            TimetableConnection("WAE", "CHX", ConnectionType.TRAIN, 1040, 1045, "SE1000", "LN") ))
        ))
      )
    )

    actual should be (expected)
  }


  it should "transfer patterns with latest departure" in {
    val timetable = List(
      TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1000, 1020, "SE1000", "LN"),
      TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1030, 1040, "SE2000", "LN"),
      TimetableConnection("WAE", "CHX", ConnectionType.TRAIN, 1040, 1045, "SE2000", "LN"),
      TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1100, 1120, "SE3000", "LN"),
      TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1130, 1140, "SE4000", "LN"),
      TimetableConnection("WAE", "CHX", ConnectionType.TRAIN, 1140, 1145, "SE4000", "LN")
    )

    val scanner = new ConnectionScanAlgorithm(timetable, HashMap(), HashMap())
    val actual = scanner.getShortestPathTree("ORP")
    val expected = mutable.HashMap(
      "WAE" -> mutable.HashMap(
        1020 -> Journey(List(
          Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1000, 1020, "SE1000", "LN")))
        )),
        1040 -> Journey(List(
          Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1030, 1040, "SE2000", "LN")))
        )),
        1120 -> Journey(List(
          Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1100, 1120, "SE3000", "LN")))
        )),
        1140 -> Journey(List(
          Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1130, 1140, "SE4000", "LN")))
        ))
      ),
      "CHX" -> mutable.HashMap(
        1045 -> Journey(List(
          Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1030, 1040, "SE2000", "LN"),
            TimetableConnection("WAE", "CHX", ConnectionType.TRAIN, 1040, 1045, "SE2000", "LN")))
        )),
        1145 -> Journey(List(
          Leg(List(TimetableConnection("ORP", "WAE", ConnectionType.TRAIN, 1130, 1140, "SE4000", "LN"),
            TimetableConnection("WAE", "CHX", ConnectionType.TRAIN, 1140, 1145, "SE4000", "LN")))
        ))
      )
    )

    actual should be(expected)
  }


  /**
    * This is modelled on a real world scenario between CHX and PDW with
    * unnecessary changes at WAE. Happens at virtually any time
    */
  it should "transfer patterns without unnecessary changes at the start" in {
    val timetable = List(
      TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1010, "CS1000", "LN"),
      TimetableConnection("A", "B", ConnectionType.TRAIN, 1010, 1015, "CS1001", "LN"),
      TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS1001", "LN")
    )

    val interchange = HashMap(
      "A" -> 1,
      "B" -> 1
    )

    val scanner = new ConnectionScanAlgorithm(timetable, HashMap(), interchange)
    val actual = scanner.getJourney("A", "C", 900)
    val expected = Journey(List(
      Leg(List(
        TimetableConnection("A", "B", ConnectionType.TRAIN, 1010, 1015, "CS1001", "LN"),
        TimetableConnection("B", "C", ConnectionType.TRAIN, 1020, 1045, "CS1001", "LN")
      ))
    ))
    actual should be(Some(expected))
  }


  /**
    * This is modelled on MYB -> WWW at 20:00 on a weekday. It puts you on a
    * train to Haddenham when you could just wait an extra 3 mins at MYB.
    *
    * The connection from MYB to Haddenham actually has less calling points
    * but it doesn't matter as it still connects to the MYB service.
    */
  it should "transfer patterns without unnecessary changes at the start 2" in {
    val timetable = List(
      TimetableConnection("A", "B", ConnectionType.TRAIN, 1000, 1010, "CS1000", "LN"),
      TimetableConnection("B", "C", ConnectionType.TRAIN, 1011, 1012, "CS1000", "LN"),
      TimetableConnection("A", "C", ConnectionType.TRAIN, 1005, 1015, "CS1001", "LN"),
      TimetableConnection("C", "D", ConnectionType.TRAIN, 1020, 1045, "CS1001", "LN")
    )

    val interchange = HashMap(
      "C" -> 1
    )

    val scanner = new ConnectionScanAlgorithm(timetable, HashMap(), interchange)
    val actual = scanner.getJourney("A", "D", 900)
    val expected = Journey(List(
      Leg(List(
        TimetableConnection("A", "C", ConnectionType.TRAIN, 1005, 1015, "CS1001", "LN"),
        TimetableConnection("C", "D", ConnectionType.TRAIN, 1020, 1045, "CS1001", "LN")
      ))
    ))
    actual should be(Some(expected))
  }
}
