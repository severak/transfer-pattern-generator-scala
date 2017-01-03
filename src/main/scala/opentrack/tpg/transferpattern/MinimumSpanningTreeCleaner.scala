package opentrack.tpg.transferpattern

import opentrack.tpg.journey.{Journey, Leg, MST, Service}

/**
  * Created by linus on 28/11/16.
  */
class MinimumSpanningTreeCleaner(services: Map[Service, Leg]) {

  def cleanTree(tree: MST): Set[TransferPattern] = {
    tree
      .values
      .flatMap(_.values)
      .map(getTransferPattern)
      .toSet
  }

  private def getTransferPattern(journey: Journey) = {
    val legs = journey.legs
      .zipWithIndex
      .map { case (legI, i) =>
        // if it's the last leg skip
        if (i + 1 == journey.legs.size) legI
        // otherwise check if the service of the next leg also stops at the origin of the current leg, if so we may as well just get on that train
        else {
          val legJ = journey.legs(i + 1) // todo this could be a for loop iterating back from the last leg, in theory there could be more than one redundant leg

          legJ.service match {
            // check whether the next service also stops at the origin of this leg, if so we don't need this leg
            case Some(serviceId) => services(serviceId).getReplacement(legI, legJ.destination)
            // if the next leg doesn't have a service (it's probably a transfer) so we need this leg
            case _ => legI
          }
        }
      }
      .groupBy(_.service)      // group the legs by service
      .map(_._2.head)          // take the first service as it will be the new leg if there is one
      .toList                  // make it a list... because Scala
      .sortBy(_.departureTime) // sort it again... because Scala

    val j = Journey(legs)

    // note we use the original origin and destination as the group by service destroys the transfer legs
    TransferPattern(journey.origin + journey.destination, j.hash)
  }

}
