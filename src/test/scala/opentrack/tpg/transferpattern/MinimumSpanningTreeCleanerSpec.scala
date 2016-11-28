package opentrack.tpg.transferpattern

import opentrack.tpg.journey.{ConnectionType, Journey, Leg, TimetableConnection}
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

}
