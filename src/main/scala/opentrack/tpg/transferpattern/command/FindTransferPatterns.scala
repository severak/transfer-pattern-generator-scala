package opentrack.tpg.transferpattern.command

import java.util.concurrent.Executors

import opentrack.tpg.journey.repository.{ConnectionRepository, StationRepository}
import opentrack.tpg.planner.ConnectionScanAlgorithm
import opentrack.tpg.transferpattern.repository.TransferPatternRepository

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * Created by linus on 08/10/16.
  */
object FindTransferPatterns {

  def apply(patternRepository: TransferPatternRepository, stationRepository: StationRepository, connectionRepository: ConnectionRepository) = {
    implicit val exec = ExecutionContext.fromExecutor(Executors.newCachedThreadPool)

    val scanDate = Await.result(patternRepository.nextScanDate, 2 seconds)
    val stations = Await.result(stationRepository.stations, 5 seconds)
    val interchange = Await.result(stationRepository.interchange, 5 seconds)

    val timetable = connectionRepository.getTimetableConnections(scanDate)
    val nonTimetable = connectionRepository.getNonTimetableConnections(scanDate)
    var numDone = 0

    val futureOptions =
      for (station <- stations.par) yield {
        val csa = new ConnectionScanAlgorithm(timetable, nonTimetable, interchange)

        numDone += 1
        println(s"Done $station ($numDone of ${stations.size})")

        patternRepository.storeTransferPatterns(station, csa.getShortestPathTree(station))
      }


    Await.result(Future.sequence(patternRepository.updateLastScanDate(scanDate) :: futureOptions.toList.flatten), 60 seconds)
  }

}
