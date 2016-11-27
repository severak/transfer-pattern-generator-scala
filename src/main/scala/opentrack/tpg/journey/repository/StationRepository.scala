package opentrack.tpg.journey.repository

import opentrack.tpg.journey.{Duration, Interchange, Station}

import com.github.mauricio.async.db.{Connection, RowData}
import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext

import scala.concurrent.Future

/**
  * Created by linus on 08/10/16.
  */
class StationRepository(db: Connection) {

  lazy val stations: Future[List[Station]] = {
    db.sendQuery("SELECT stop_code FROM stops WHERE stop_code != ''").map(queryResult => queryResult.rows match {
      case Some(rows) => rows.map(row => row("stop_code").asInstanceOf[String]).toList
      case None => List()
    })
  }

  lazy val interchange: Future[Interchange] = {
    db.sendQuery("SELECT from_stop_id, min_transfer_time FROM transfers").map(queryResult => queryResult.rows match {
      case Some(rows) => rows.map(rowToKeyValue).toMap[Station, Duration]
      case None => Map()
    })
  }

  private def rowToKeyValue(row: RowData) = {
    row("from_stop_id").asInstanceOf[String] -> row("min_transfer_time").asInstanceOf[Int]
  }

}
