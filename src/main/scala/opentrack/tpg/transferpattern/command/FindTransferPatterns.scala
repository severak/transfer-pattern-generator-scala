package opentrack.tpg.transferpattern.command

import opentrack.tpg.journey.repository.{ConnectionRepository, StationRepository}
import opentrack.tpg.planner.ConnectionScanAlgorithm
import opentrack.tpg.transferpattern.repository.TransferPatternRepository

/**
  * Created by linus on 08/10/16.
  */
class FindTransferPatterns(patternRepository: TransferPatternRepository, stationRepository: StationRepository, connectionRepository: ConnectionRepository) {

  def run() = {

    val scanDate = patternRepository.nextScanDate
    val stations = stationRepository.stations
    val interchange = stationRepository.interchange

    val timetable = connectionRepository.getTimetableConnections(scanDate)
    val nonTimetable = connectionRepository.getNonTimetableConnections(scanDate)


    stations.par.foreach { case (station: String) =>
      val csa = new ConnectionScanAlgorithm(timetable, nonTimetable, interchange)

      patternRepository.storeTransferPatterns(station, csa.getShortestPathTree(station))

      println("Done " + station)
    }

    patternRepository.updateLastScanDate(scanDate)
  }

}
