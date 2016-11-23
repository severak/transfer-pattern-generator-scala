package opentrack.tpg.journey.repository

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, TextStyle}
import java.util.Locale

import opentrack.tpg.journey._
import scalikejdbc._

case class TripStop(stop: Station, departureTime: Time, arrivalTime: Time, service: Service)

/**
  * Created by linus on 08/10/16.
  */
class ConnectionRepository() {

  val stopBuilder = (rs: WrappedResultSet) => TripStop(
    rs.string("parent_station"),
    rs.int("departure_time"),
    rs.int("arrival_time"),
    rs.string("train_uid")
  )

  def getTimetableConnections(dateTime: LocalDate): TimetableSchedule = DB localTx { implicit session =>
    val dow = dateTime.getDayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toLowerCase
    val date = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)

    val stops = sql"""
    SELECT TIME_TO_SEC(departure_time) as departure_time, TIME_TO_SEC(arrival_time) as arrival_time, parent_station, train_uid
    FROM stop_times
    JOIN trips USING(trip_id)
    JOIN calendar USING(service_id)
    JOIN stops USING(stop_id)
    WHERE ${date} BETWEEN start_date AND end_date
    AND ${SQLSyntax.createUnsafely(dow)} = 1
    ORDER BY trip_id, stop_sequence
    """.map(stopBuilder).list.apply()

    val connections =
      for (
        (service, trip: List[TripStop]) <- stops.groupBy(_.service);
        i <- 0 until trip.length - 1 if trip(i).departureTime <= trip(i + 1).arrivalTime
      ) yield {
        TimetableConnection(
          trip(i).stop,
          trip(i + 1).stop,
          ConnectionType.TRAIN,
          trip(i).departureTime,
          trip(i + 1).arrivalTime,
          trip(i).service,
          "LN"
        )
      }

    connections.toList.sortBy(_.arrivalTime)
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
