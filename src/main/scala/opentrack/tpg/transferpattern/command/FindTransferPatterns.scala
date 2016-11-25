package opentrack.tpg.transferpattern.command

import opentrack.tpg.journey.repository.{ConnectionRepository, StationRepository}
import opentrack.tpg.planner.ConnectionScanAlgorithm
import opentrack.tpg.transferpattern.repository.TransferPatternRepository

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * Created by linus on 08/10/16.
  */
object FindTransferPatterns {

  def apply(patternRepository: TransferPatternRepository, stationRepository: StationRepository, connectionRepository: ConnectionRepository) = {

    val scanDate = Await.result(patternRepository.nextScanDate, 5 seconds)
    val stations = stationRepository.stations
    val interchange = stationRepository.interchange

    val timetable = connectionRepository.getTimetableConnections(scanDate)
    val nonTimetable = connectionRepository.getNonTimetableConnections(scanDate)
    var numDone = 0

    for (station <- stations.par) yield {
      val csa = new ConnectionScanAlgorithm(timetable, nonTimetable, interchange)

      numDone += 1
      println(s"Done $station ($numDone of ${stations.size})")

      patternRepository.storeTransferPatterns(station, csa.getShortestPathTree(station))
    }

    Await.result(patternRepository.updateLastScanDate(scanDate), 5 seconds)
  }

}
