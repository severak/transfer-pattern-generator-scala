package opentrack.tpg.transferpattern

import opentrack.tpg.journey._
import opentrack.tpg.planner.ConnectionScanAlgorithm
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.immutable.HashMap
import scala.collection.mutable

/**
  * Created by linus on 30/09/16.
  */
class MinimumSpanningTreeCleanerSpec extends FlatSpec with Matchers {

  "MinimumSpanningTreeCleaner" should "remove unnecessary legs" in {
    val services = Map(
      "LN0000" -> Leg(List(
        TimetableConnection("HIB", "TON", ConnectionType.TRAIN, 900, 915, "LN0000", "LN")
      )),
      "LN0001" -> Leg(List(
        TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1015, 1035, "LN0001", "LN"),
        TimetableConnection("SEV", "ORP", ConnectionType.TRAIN, 1035, 1045, "LN0001", "LN")
      )),
      "LN0002" -> Leg(List(
        TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1010, 1030, "LN0002", "LN")
      ))
    )

    val timetable = List(
      TimetableConnection("HIB", "TON", ConnectionType.TRAIN, 900, 915, "LN0000", "LN"),
      TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1010, 1030, "LN0002", "LN"),
      TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1015, 1035, "LN0001", "LN"),
      TimetableConnection("SEV", "ORP", ConnectionType.TRAIN, 1035, 1045, "LN0001", "LN")
    )


    val scanner = new ConnectionScanAlgorithm(timetable, HashMap(), HashMap())
    val actual = scanner.getShortestPathTree("HIB")
    val expectedWithUnnecessaryChange = mutable.HashMap(
      "TON" -> mutable.HashMap(
        915  -> Journey(List(
          Leg(List(
            TimetableConnection("HIB", "TON", ConnectionType.TRAIN, 900, 915, "LN0000", "LN")
          ))
        ))
      ),
      "SEV" -> mutable.HashMap(
        1030 -> Journey(List(
          Leg(List(
            TimetableConnection("HIB", "TON", ConnectionType.TRAIN, 900, 915, "LN0000", "LN")
          )),
          Leg(List(
            TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1010, 1030, "LN0002", "LN")
          ))
        ))
      ),
      "ORP" -> mutable.HashMap(
        1045 -> Journey(List(
          Leg(List(
            TimetableConnection("HIB", "TON", ConnectionType.TRAIN, 900, 915, "LN0000", "LN")
          )),
          Leg(List(
            TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1010, 1030, "LN0002", "LN")
          )),
          Leg(List(
            TimetableConnection("SEV", "ORP", ConnectionType.TRAIN, 1035, 1045, "LN0001", "LN")
          ))
        ))
      )
    )

    actual should be (expectedWithUnnecessaryChange)

    val mstCleaner = new MinimumSpanningTreeCleaner(services)
    val actualCleaned = mstCleaner.cleanTree(actual)
    val expectedCleaned = Set(
      TransferPattern("HIBTON", "HIBTON"),
      TransferPattern("HIBSEV", "HIBTONTONSEV"),
      TransferPattern("HIBORP", "HIBTONTONORP")
    )

    actualCleaned should be (expectedCleaned)
  }

  it should "include the original origin if the first leg is a transfer" in {
    val services = Map(
      "LN0000" -> Leg(List(
        TimetableConnection("HIB", "TON", ConnectionType.TRAIN, 900, 915, "LN0000", "LN")
      )),
      "LN0001" -> Leg(List(
        TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1015, 1035, "LN0001", "LN"),
        TimetableConnection("SEV", "ORP", ConnectionType.TRAIN, 1035, 1045, "LN0001", "LN")
      )),
      "LN0002" -> Leg(List(
        TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1010, 1030, "LN0002", "LN")
      ))
    )

    val timetable = List(
      TimetableConnection("HIB", "TON", ConnectionType.TRAIN, 900, 915, "LN0000", "LN"),
      TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1010, 1030, "LN0002", "LN"),
      TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1015, 1035, "LN0001", "LN"),
      TimetableConnection("SEV", "ORP", ConnectionType.TRAIN, 1035, 1045, "LN0001", "LN")
    )


    val nonTimetable = HashMap("TBW" -> List(NonTimetableConnection("TBW", "HIB", ConnectionType.WALK, 5, 0, 99999)))
    val scanner = new ConnectionScanAlgorithm(timetable, nonTimetable, HashMap())
    val actual = scanner.getShortestPathTree("TBW")
    val expectedWithUnnecessaryChange = mutable.HashMap(
      "TON" -> mutable.HashMap(
        915  -> Journey(List(
          Leg(List(NonTimetableConnection("TBW", "HIB", ConnectionType.WALK, 5, 0, 99999))),
          Leg(List(
            TimetableConnection("HIB", "TON", ConnectionType.TRAIN, 900, 915, "LN0000", "LN")
          ))
        ))
      ),
      "SEV" -> mutable.HashMap(
        1030 -> Journey(List(
          Leg(List(NonTimetableConnection("TBW", "HIB", ConnectionType.WALK, 5, 0, 99999))),
          Leg(List(
            TimetableConnection("HIB", "TON", ConnectionType.TRAIN, 900, 915, "LN0000", "LN")
          )),
          Leg(List(
            TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1010, 1030, "LN0002", "LN")
          ))
        ))
      ),
      "ORP" -> mutable.HashMap(
        1045 -> Journey(List(
          Leg(List(NonTimetableConnection("TBW", "HIB", ConnectionType.WALK, 5, 0, 99999))),
          Leg(List(
            TimetableConnection("HIB", "TON", ConnectionType.TRAIN, 900, 915, "LN0000", "LN")
          )),
          Leg(List(
            TimetableConnection("TON", "SEV", ConnectionType.TRAIN, 1010, 1030, "LN0002", "LN")
          )),
          Leg(List(
            TimetableConnection("SEV", "ORP", ConnectionType.TRAIN, 1035, 1045, "LN0001", "LN")
          ))
        ))
      ),
      "HIB" -> mutable.HashMap(
        901 -> Journey(List(
          Leg(List(NonTimetableConnection("TBW", "HIB", ConnectionType.WALK, 5, 0, 99999)))
        )),
        6 -> Journey(List(
          Leg(List(NonTimetableConnection("TBW", "HIB", ConnectionType.WALK, 5, 0, 99999)))
        ))
      )
    )

    actual should be (expectedWithUnnecessaryChange)

    val mstCleaner = new MinimumSpanningTreeCleaner(services)
    val actualCleaned = mstCleaner.cleanTree(actual)
    val expectedCleaned = Set(
      TransferPattern("TBWHIB", ""),
      TransferPattern("TBWTON", "HIBTON"),
      TransferPattern("TBWSEV", "HIBTONTONSEV"),
      TransferPattern("TBWORP", "HIBTONTONORP")
    )

    actualCleaned should be (expectedCleaned)
  }


  it should "condense multiple unnecessary legs" in {
    val services = Map(
      "LN0000" -> Leg(List(
        TimetableConnection("CHX", "WAE", ConnectionType.TRAIN, 900, 915, "LN0000", "LN"),
        TimetableConnection("WAE", "LBG", ConnectionType.TRAIN, 916, 920, "LN0000", "LN"),
        TimetableConnection("LBG", "ORP", ConnectionType.TRAIN, 921, 925, "LN0000", "LN")
      )),
      "LN0001" -> Leg(List(
        TimetableConnection("CHX", "WAE", ConnectionType.TRAIN, 855, 900, "LN0001", "LN"),
        TimetableConnection("WAE", "LBG", ConnectionType.TRAIN, 901, 906, "LN0001", "LN")
      )),
      "LN0002" -> Leg(List(
        TimetableConnection("CHX", "WAE", ConnectionType.TRAIN, 850, 855, "LN0002", "LN")
      ))
    )

    val expectedWithUnnecessaryChange = mutable.HashMap(
      "ORP" -> mutable.HashMap(
        925 -> Journey(List(
          Leg(List(
            TimetableConnection("CHX", "WAE", ConnectionType.TRAIN, 850, 855, "LN0002", "LN")
          )),
          Leg(List(
            TimetableConnection("WAE", "LBG", ConnectionType.TRAIN, 901, 906, "LN0001", "LN")
          )),
          Leg(List(
            TimetableConnection("LBG", "ORP", ConnectionType.TRAIN, 921, 925, "LN0000", "LN")
          ))
        ))
      )
    )

    val mstCleaner = new MinimumSpanningTreeCleaner(services)
    val actualCleaned = mstCleaner.cleanTree(expectedWithUnnecessaryChange)
    val expectedCleaned = Set(
        TransferPattern("CHXORP", "CHXORP")
    )

    actualCleaned should be (expectedCleaned)
  }

}
