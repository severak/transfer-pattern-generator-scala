package opentrack.tpg.transferpattern.repository

import java.time.LocalDate

import org.joda.time.{LocalDate => JodaDate}
import com.github.mauricio.async.db.Connection
import opentrack.tpg.journey.{MST, Station}
import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext
import opentrack.tpg.transferpattern.TransferPattern

/**
  * Created by linus on 08/10/16.
  */
class TransferPatternRepository(db: Connection) {

  def nextScanDate = {
    db.sendQuery("SELECT date FROM last_transfer_pattern_scan").map(queryResult => queryResult.rows match {
      case Some(rows) => LocalDate.parse(rows.head("date").asInstanceOf[JodaDate].toString("yyyy-MM-dd")).plusDays(1)
      case None => LocalDate.now().plusDays(1)
    })
  }

  def updateLastScanDate(date: LocalDate) = {
    db.sendPreparedStatement("UPDATE last_transfer_pattern_scan SET date = ?", Array(JodaDate.parse(date.toString)))
  }

  def storeTransferPatterns(patterns: Set[TransferPattern]) = {
    val inserts = patterns.map(tp => s"('${tp.journey}','${tp.pattern}')")

    if (inserts.nonEmpty) {
      Some(db.sendQuery("INSERT IGNORE INTO transfer_patterns VALUES " + inserts.toSet.mkString(",")))
    }
    else {
      None
    }
  }

}
