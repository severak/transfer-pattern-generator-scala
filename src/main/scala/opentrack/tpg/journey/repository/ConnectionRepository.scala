package opentrack.tpg.journey.repository

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, TextStyle}
import java.util.Locale

import opentrack.tpg.journey._
import scalikejdbc._

import scala.collection.immutable.HashMap

/**
  * Created by linus on 08/10/16.
  */
class ConnectionRepository() {

  val connectionBuilder = (rs: WrappedResultSet) => TimetableConnection(
    rs.string("origin"),
    rs.string("destination"),
    ConnectionType.withName(rs.string("mode").toLowerCase),
    rs.int("departure_time"),
    rs.int("arrival_time"),
    rs.string("service"),
    rs.string("operator")
  )

  def getTimetableConnections(dateTime: LocalDate): TimetableSchedule = DB localTx { implicit session =>
    val dow = dateTime.getDayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toLowerCase
    val date = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)

    sql"""
    SELECT TIME_TO_SEC(departure_time) as departure_time, TIME_TO_SEC(arrival_time) as arrival_time, origin, destination, service, operator, type as mode
    FROM timetable_connection
    WHERE ${date} BETWEEN start_date AND end_date
    AND departure_time >= "05:00"
    AND ${SQLSyntax.createUnsafely(dow)} = 1
    ORDER BY arrival_time
    """.map(connectionBuilder).list.apply()
  }

  val nonTimetableConnectionBuilder = (rs: WrappedResultSet) => NonTimetableConnection(
    rs.string("origin"),
    rs.string("destination"),
    ConnectionType.withName(rs.string("mode").toLowerCase),
    rs.int("duration"),
    rs.int("start_time"),
    rs.int("end_time")
  )

  def getNonTimetableConnections(dateTime: LocalDate): NonTimetableSchedule = DB localTx { implicit session =>
    val dow = dateTime.getDayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toLowerCase
    val date = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)

    sql"""
    SELECT
        from_stop_id as origin,
        to_stop_id as destination,
        link_secs as duration,
        mode,
        TIME_TO_SEC(start_time) as start_time,
        TIME_TO_SEC(end_time) as end_time
    FROM links
    WHERE ${date} BETWEEN start_date AND end_date
    AND ${SQLSyntax.createUnsafely(dow)} = 1
    """.map(nonTimetableConnectionBuilder).list.apply().foldLeft(Map[Station, List[NonTimetableConnection]]()) { (result: NonTimetableSchedule, connection: NonTimetableConnection) =>
      result.get(connection.origin) match {
        case Some(l) => result + (connection.origin -> (l :+ connection))
        case None => result + (connection.origin -> List(connection))
      }
    }
  }

}
