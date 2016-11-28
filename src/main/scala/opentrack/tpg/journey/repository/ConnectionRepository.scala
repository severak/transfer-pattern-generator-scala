package opentrack.tpg.journey.repository

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, TextStyle}
import java.util.Locale

import com.github.mauricio.async.db.RowData
import com.github.mauricio.async.db.Connection
import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext

import opentrack.tpg.journey._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


case class TripStop(stop: Station, departureTime: Time, arrivalTime: Time, service: Service)

/**
  * Created by linus on 08/10/16.
  */
class ConnectionRepository(db: Connection) {

  private def rowToTripStop(row: RowData) = {
    TripStop(
      row("parent_station").asInstanceOf[String],
      row("departure_time").asInstanceOf[Long].toInt,
      row("arrival_time").asInstanceOf[Long].toInt,
      row("train_uid").asInstanceOf[String]
    )
  }

  def getTimetableConnections(dateTime: LocalDate): (Map[Service, Leg], TimetableSchedule) = {
    val dow = dateTime.getDayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toLowerCase
    val date = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)

    val future = db.sendQuery(s"""
      SELECT TIME_TO_SEC(departure_time) as departure_time, TIME_TO_SEC(arrival_time) as arrival_time, parent_station, train_uid
      FROM stop_times
      JOIN trips USING(trip_id)
      JOIN calendar USING(service_id)
      JOIN stops USING(stop_id)
      WHERE "${date}" BETWEEN start_date AND end_date
      AND ${dow} = 1
      ORDER BY trip_id, stop_sequence
    """).map(queryResult => queryResult.rows match {
      case Some(rows) => rows.map(rowToTripStop).toList
      case None => List()
    })

    val stops = Await.result(future, 60 seconds)

    val connections =
      for (
        (service, trip) <- stops.groupBy(_.service);
        i <- 0 until trip.length - 1 if trip(i).departureTime <= trip(i + 1).arrivalTime
      ) yield TimetableConnection(
        trip(i).stop,
        trip(i + 1).stop,
        ConnectionType.TRAIN,
        trip(i).departureTime,
        trip(i + 1).arrivalTime,
        service,
        "LN"
      )

    val services =
      for (
        (service, trip) <- connections.groupBy(_.service)
      ) yield service -> Leg(trip.toList)

    (services, connections.toList.sortBy(_.arrivalTime))
  }

  def getNonTimetableConnections(dateTime: LocalDate): NonTimetableSchedule = {
    val dow = dateTime.getDayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toLowerCase
    val date = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val future = db.sendQuery(s"""
      SELECT
          from_stop_id as origin,
          to_stop_id as destination,
          link_secs as duration,
          mode,
          TIME_TO_SEC(start_time) as start_time,
          TIME_TO_SEC(end_time) as end_time
      FROM links
      WHERE "${date}" BETWEEN start_date AND end_date
      AND ${dow} = 1
      """
    ).map(queryResult => queryResult.rows match {
      case Some(rows) => rows.map(rowToNonTimetableConnection).toList
      case None => List()
    })

    val connections = Await.result(future, 10 seconds)

    connections.foldLeft(Map[Station, List[NonTimetableConnection]]()) { (result: NonTimetableSchedule, connection: NonTimetableConnection) =>
      result.get(connection.origin) match {
        case Some(l) => result + (connection.origin -> (l :+ connection))
        case None => result + (connection.origin -> List(connection))
      }
    }
  }

  private def rowToNonTimetableConnection(row: RowData) = {
    NonTimetableConnection(
      row("origin").asInstanceOf[String],
      row("destination").asInstanceOf[String],
      ConnectionType.withName(row("mode").asInstanceOf[String].toLowerCase),
      row("duration").asInstanceOf[Int],
      row("start_time").asInstanceOf[Long].toInt,
      row("end_time").asInstanceOf[Long].toInt
    )
  }

}
