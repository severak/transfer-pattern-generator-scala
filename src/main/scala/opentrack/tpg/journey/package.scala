package opentrack.tpg

import scala.collection.immutable.Map
import scala.collection.mutable

/**
  * Created by linus on 30/09/16.
  */
package object journey {

  type Station = String
  type Duration = Int
  type Time = Int
  type Service = String
  type Operator = String
  type JourneyHash = String
  type Interchange = Map[Station, Duration]
  type NonTimetableSchedule = Map[Station, List[NonTimetableConnection]]
  type TimetableSchedule = List[TimetableConnection]
  type MST = mutable.HashMap[String, mutable.HashMap[Int, Journey]]

  object ConnectionType extends Enumeration {
    val TRAIN = Value("train")
    val BUS = Value("bus")
    val WALK = Value("walk")
    val TUBE = Value("tube")
    val METRO = Value("metro")
    val TRANSFER = Value("transfer")
    val UNKNOWN = Value("")
  }
}
