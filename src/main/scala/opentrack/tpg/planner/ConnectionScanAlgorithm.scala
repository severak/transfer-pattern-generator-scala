package opentrack.tpg.planner

import opentrack.tpg.journey._

import scala.annotation.tailrec
import scala.collection.mutable

/**
  * Created by linus on 30/09/16.
  */
class ConnectionScanAlgorithm(timetable: TimetableSchedule, nonTimetable: NonTimetableSchedule, interchange: Interchange) {

  var connections: mutable.HashMap[Station, Connection] = mutable.HashMap()
  var arrivals: mutable.HashMap[Station, Time] = mutable.HashMap()

  def getJourney(origin: Station, destination: Station, departure: Time) = {
    @tailrec def latestJourney(journey: Journey): Journey = {
      getJourneyAfter(origin, destination, journey.departureTime + 1) match {
        case None => journey
        case Some(j) if j.departureTime == 0 => journey
        case Some(j) => latestJourney(j)
      }
    }

    getJourneyAfter(origin, destination, departure) match {
      case None => None
      case Some(j) => Some(latestJourney(j))
    }
  }

  private def getJourneyAfter(origin: Station, destination: Station, departure: Time): Option[Journey] = {
    arrivals = mutable.HashMap(origin -> departure)
    connections = mutable.HashMap()

    setFastestConnections(origin, destination)
    getJourneyFromConnections(origin, destination)
  }

  private def setFastestConnections(origin: Station, destination: Station) = {
    checkForBetterNonTimetableConnections(origin, arrivals.get(origin).head)

    timetable.foreach(connection => {
      if (canGetToConnection(connection) && connectionIsBetter(connection)) {
        this.connections.put(connection.destination, connection)
        this.arrivals.put(connection.destination, connection.arrivalTime)

        checkForBetterNonTimetableConnections(connection.destination, connection.arrivalTime)
      }
    })
  }

  private def checkForBetterNonTimetableConnections(origin: Station, arrival: Time) = {
    nonTimetable.getOrElse(origin, List()).foreach(connection => {
      if (connection.isAvailableAt(arrival) && connectionIsBetter(connection)) {
        this.connections.put(connection.destination, connection)
        this.arrivals.put(connection.destination, arrivals.get(connection.origin).head + connection.duration)
      }
    })
  }

  private def connectionIsBetter(c: Connection) = {
    lazy val comparisonTime: Time = c match {
      case tt: TimetableConnection => tt.arrivalTime
      case ntt: NonTimetableConnection => arrivals.get(c.origin).head + c.duration
    }

    arrivals.get(c.destination) match {
      case None => true
      case Some(time) => time >= comparisonTime
    }
  }

  private def canGetToConnection(c: TimetableConnection) = {
    lazy val interchangeTime = interchange.get(c.origin) match {
      case None => 0
      case Some(duration) => connections.get(c.origin) match {
        case Some(otherConnection) if otherConnection.requiresInterchangeWith(c) => duration
        case _ => 0
      }
    }

    arrivals.get(c.origin) match {
      case None => false
      case Some(time) => time + interchangeTime <= c.departureTime
    }
  }

  private def getJourneyFromConnections(origin: Station, destination: Station): Option[Journey] = {
    @tailrec def getLegs(cOpt: Option[Connection], leg: List[Connection], journey: List[Leg]): Option[List[Leg]] = cOpt match {
      case None => None
      case Some(c) if c.origin == origin && leg.nonEmpty && c.requiresInterchangeWith(leg.head) => Some(Leg(List(c)) :: Leg(leg) :: journey)
      case Some(c) if c.origin == origin => Some(Leg(c :: leg) :: journey)
      case Some(c) =>
        if (leg.nonEmpty && c.requiresInterchangeWith(leg.head))
          getLegs(connections.get(c.origin), List(c), Leg(leg) :: journey)
        else
          getLegs(connections.get(c.origin), c :: leg, journey)
    }

    getLegs(connections.get(destination), List(), List()) match {
      case None => None
      case Some(legs) => Some(Journey(legs))
    }
  }
}
