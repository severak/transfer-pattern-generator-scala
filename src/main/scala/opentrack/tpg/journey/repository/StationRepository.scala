package opentrack.tpg.journey.repository

import opentrack.tpg.journey.{Duration, Interchange, Station}
import scalikejdbc._

/**
  * Created by linus on 08/10/16.
  */
class StationRepository() {
  val crsOnly = (rs: WrappedResultSet) => rs.string("stop_code")

  lazy val stations: List[Station] = DB localTx { implicit session =>
    sql"SELECT stop_code FROM stops WHERE stop_code != ''".map(crsOnly).list.apply()
  }

  val interchangeMap = (rs: WrappedResultSet) => rs.string("from_stop_id") -> rs.int("min_transfer_time")

  lazy val interchange: Interchange = DB localTx { implicit session =>
    sql"SELECT from_stop_id, min_transfer_time FROM transfers".map(interchangeMap).list.apply().toMap[Station, Duration]
  }

}
