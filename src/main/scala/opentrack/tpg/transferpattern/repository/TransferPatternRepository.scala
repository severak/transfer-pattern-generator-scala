package opentrack.tpg.transferpattern.repository

import java.time.{LocalDate}
import java.time.format.DateTimeFormatter

import scalikejdbc._

/**
  * Created by linus on 08/10/16.
  */
class TransferPatternRepository(db: DB) {

  def nextScanDate() = DB localTx { implicit session =>
    val result = sql"SELECT date + INTERVAL 1 DAY as date FROM last_transfer_pattern_scan".map(rs => rs.string("date")).single.apply()

    LocalDate.parse(result.get, DateTimeFormatter.ISO_LOCAL_DATE)
  }

}
