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

  lazy val service: Option[Service] = connections.head match {
    case c: NonTimetableConnection => None
    case c: TimetableConnection => Some(c.service)
  }

  /**
   * If this service stops at the origin of the given leg and it does not depart the origin station any earlier, then
   * return a segment of this service between the leg origin and given destination. Otherwise return the given leg.
   *
   * This is used to check whether the given leg can be replaced entirely by this service.
   *
   * @param leg
   * @param destination
   * @return
   */
  def getReplacement(leg: Leg, destination: Station): Leg = {
    val newConnections = connections
      .dropWhile { case (c: TimetableConnection) =>
        c.origin != leg.origin || c.departureTime < leg.departureTime
      }
      .takeWhile { case (c: TimetableConnection) =>
        c.origin != destination
      }

    if (newConnections.isEmpty || newConnections.last.destination != destination) leg
    else Leg(newConnections)
  }

  lazy val timetableConnections: List[TimetableConnection] = {
    connections.flatMap { case t: TimetableConnection => Some(t) }
  }

  override def requiresInterchangeWith(connection: Connection): Boolean = true
}

