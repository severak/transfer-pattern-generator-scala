package opentrack.tpg.journey

/**
  * Created by linus on 30/09/16.
  */
case class Journey(legs: List[Leg]) {
  require(legs.nonEmpty, "A journey must have at least one leg")

  lazy val origin = legs.head.origin
  lazy val destination = legs.last.destination
  lazy val duration: Duration = arrivalTime - departureTime

  lazy val departureTime = {
    val (ntLegs, ttLegs) = legs.span(_.isTransfer)

    ttLegs.headOption.map(leg => leg.departureTime - ntLegs.map(_.duration).sum).sum
  }

  lazy val arrivalTime = {
    val (ntLegs, ttLegs) = legs.reverse.span(_.isTransfer)

    ttLegs.headOption.map(leg => leg.arrivalTime + ntLegs.map(_.duration).sum).sum
  }

  val hash = {
    legs.filter(!_.isTransfer).map(l => l.origin + l.destination).mkString
  }

  val hasTimetableLegs = hash.nonEmpty

}
