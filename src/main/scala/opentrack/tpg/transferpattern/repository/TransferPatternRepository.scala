package opentrack.tpg.transferpattern.repository

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import opentrack.tpg.journey.{Journey, MST, Station}
import scalikejdbc._

/**
  * Created by linus on 08/10/16.
  */
class TransferPatternRepository() {

  def nextScanDate() = DB localTx { implicit session =>
    val result = sql"SELECT date + INTERVAL 1 DAY as date FROM last_transfer_pattern_scan".map(rs => rs.string("date")).single.apply()

    LocalDate.parse(result.get, DateTimeFormatter.ISO_LOCAL_DATE)
  }

  def updateLastScanDate(date: LocalDate) = DB localTx { implicit session =>
    sql"UPDATE last_transfer_pattern_scan SET date = ${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}".update.apply()
  }

  def storeTransferPatterns(station: Station, patterns: MST) = DB localTx { implicit session =>
    val storedPatterns = getStoredPatterns(station)

    for (
      (station, journeys) <- patterns;
      (time, journey) <- journeys if !storedPatterns.contains(journey.hash) && journey.legs.length < 8 && journey.duration < 60000
    ) {
      val id = sql"INSERT INTO transfer_pattern VALUES (null, ${journey.origin}, ${journey.destination}, ${journey.duration}, SEC_TO_TIME(${journey.departureTime}), SEC_TO_TIME(${journey.departureTime}))".updateAndReturnGeneratedKey.apply()

      for (leg <- journey.legs) {
        sql"INSERT INTO transfer_pattern_leg VALUES (null, ${id}, ${leg.origin}, ${leg.destination})".update.apply()
      }
    }
  }

  def getStoredPatterns(station: Station) = DB localTx { implicit session =>
    sql"""
       SELECT concat(tp.origin, tp.destination, group_concat(leg.origin, leg.destination separator '')) as hash, journey_duration
       FROM transfer_pattern tp
       JOIN transfer_pattern_leg leg ON leg.transfer_pattern = tp.id
       WHERE tp.origin = $station
       GROUP BY tp.id
    """.map(rs => rs.string("hash") -> true).list.apply().toMap
  }

}
