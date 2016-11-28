package opentrack.tpg.transferpattern

import opentrack.tpg.journey.{Journey, Leg, MST, Service}

/**
  * Created by linus on 28/11/16.
  */
class MinimumSpanningTreeCleaner(services: Map[Service, Leg]) {

  def cleanTree(tree: MST) = {
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
        if (i + 1 == journey.legs.size || journey.legs(i + 1).isTransfer) journey.legs(i)
        else {
          val legJ = journey.legs(i + 1)

          legJ.service match {
            case Some(serviceId) if !legI.isTransfer => services(serviceId).getReplacement(legI, legJ.destination)
            case _ => legI
          }
        }
      }
      .groupBy(_.service)
      .map(_._2.head)
      .toList
      .sortBy(_.departureTime)

    val j = Journey(legs)

    TransferPattern(j.origin + j.destination, j.hash)
  }

}
