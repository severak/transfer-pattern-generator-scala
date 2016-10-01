package opentrack.tpg

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
  type Interchange = mutable.HashMap[Station, Duration]
  type NonTimetableSchedule = mutable.HashMap[Station, List[NonTimetableConnection]]
  type TimetableSchedule = List[TimetableConnection]

  object ConnectionType extends Enumeration {
    val TRAIN, BUS, WALK, TUBE = Value
  }
}
