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
    val inserts = for (
      (station, journeys) <- patterns;
      (time, journey) <- journeys if journey.legs.length < 8
    ) yield Seq(journey.hash, journey.origin, journey.destination)

    sql"""
        INSERT IGNORE INTO pattern (id, origin, destination) VALUES (?, ?, ?)
      """.batch(inserts.toSeq: _*).apply()
  }

}
