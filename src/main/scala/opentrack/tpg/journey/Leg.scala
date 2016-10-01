package opentrack.tpg.journey

/**
  * Created by linus on 30/09/16.
  */
case class Leg(connections: List[Connection]) extends Connection {
  require(connections.nonEmpty, "A leg must have at least one connection")

  lazy val origin = connections.head.origin
  lazy val destination = connections.last.destination
  lazy val mode = connections.head.mode

  lazy val isTransfer = connections.head match {
    case c: NonTimetableConnection => true
    case c: TimetableConnection => false
  }

  lazy val departureTime = connections.head match {
    case c: NonTimetableConnection => 0
    case c: TimetableConnection => c.departureTime
  }

  lazy val arrivalTime = connections.last match {
    case c: NonTimetableConnection => 0
    case c: TimetableConnection => c.arrivalTime
  }

  lazy val duration: Duration = {
    if (isTransfer) connections.head.duration
    else arrivalTime - departureTime
  }

  override def requiresInterchangeWith(connection: Connection): Boolean = true
}

