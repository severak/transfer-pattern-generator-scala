package opentrack.tpg.journey

/**
  * Created by linus on 30/09/16.
  */
trait Connection {

  def origin: Station
  def destination: Station
  def mode: ConnectionType.Value
  def duration: Duration

  def requiresInterchangeWith(connection: Connection): Boolean
}
