package opentrack.tpg.journey

/**
  * Created by linus on 30/09/16.
  */
case class NonTimetableConnection(origin: Station, destination: Station, mode: ConnectionType.Value, duration: Duration, startTime: Time, endTime: Time) extends Connection {

  override def requiresInterchangeWith(connection: Connection): Boolean = true

  def isAvailableAt(time: Time) = startTime <= time && endTime >= time
}
