package opentrack.tpg.transferpattern.repository

import java.time.LocalDate
import org.joda.time.{LocalDate => JodaDate}

import com.github.mauricio.async.db.Connection
import opentrack.tpg.journey.{MST, Station}

import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext

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

  def storeTransferPatterns(station: Station, patterns: MST) = {
    val inserts =
      for (
        (station, journeys) <- patterns;
        (time, journey) <- journeys if journey.legs.length < 10
      ) yield s"('${journey.origin}${journey.destination}','${journey.hash}')"

    if (inserts.nonEmpty) {
      Some(db.sendQuery("INSERT IGNORE INTO transfer_patterns VALUES " + inserts.toSet.mkString(",")))
    }
    else {
      None
    }
  }

}
