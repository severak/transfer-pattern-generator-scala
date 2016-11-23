package opentrack.tpg.transferpattern.command

import opentrack.tpg.journey.repository.{ConnectionRepository, StationRepository}
import opentrack.tpg.planner.ConnectionScanAlgorithm
import opentrack.tpg.transferpattern.repository.TransferPatternRepository

import scala.collection.parallel.ForkJoinTaskSupport

/**
  * Created by linus on 08/10/16.
  */
object FindTransferPatterns {

  def apply(patternRepository: TransferPatternRepository, stationRepository: StationRepository, connectionRepository: ConnectionRepository) = {

    val scanDate = patternRepository.nextScanDate
    val stations = stationRepository.stations
    val interchange = stationRepository.interchange

    val timetable = connectionRepository.getTimetableConnections(scanDate)
    val nonTimetable = connectionRepository.getNonTimetableConnections(scanDate)
    var numDone = 0

    for (station <- stations.par) {
      val csa = new ConnectionScanAlgorithm(timetable, nonTimetable, interchange)

      patternRepository.storeTransferPatterns(station, csa.getShortestPathTree(station))

      numDone += 1
      println(s"Done $station ($numDone of ${stations.size})")
    }

    patternRepository.updateLastScanDate(scanDate)
  }

}
