package opentrack.tpg.transferpattern

import opentrack.tpg.journey.{Journey, Leg, MST, Service}

import scala.collection.mutable.ListBuffer

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
    val newLegs = ListBuffer[Leg]()
    var i = journey.legs.size - 1

    // working backwards through the legs
    while(i >= 0) {
      var legI = journey.legs(i)

      if (!legI.isTransfer) {
        val service = services(legI.service.get)
        var j = i - 1

        // check to see whether any of the earlier legs could be replaced by this leg
        while (j >= 0) {
          service.getReplacement(journey.legs(j), legI.destination) match {
            case None => {}
            case Some(replacement) =>
              legI = replacement
              i = j
          }

          j = j - 1
        }
      }

      newLegs += legI
      i = i - 1
    }

    val j = Journey(newLegs.reverse.toList)

    // note we use the original origin and destination as the group by service destroys the transfer legs
    TransferPattern(journey.origin + journey.destination, j.hash)
  }

}
