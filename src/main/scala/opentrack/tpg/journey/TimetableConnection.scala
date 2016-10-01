package opentrack.tpg.journey

/**
  * Created by linus on 30/09/16.
  */
case class TimetableConnection(origin: Station, destination: Station, mode: ConnectionType.Value, departureTime: Time, arrivalTime: Time, service: Service, operator: Operator) extends Connection {

  override def duration: Duration = arrivalTime - departureTime

  override def requiresInterchangeWith(connection: Connection): Boolean = connection match {
    case c: TimetableConnection => c.service != this.service
    case _ => true
  }
}
