package opentrack.tpg.transferpattern.command

import java.util.concurrent.Executors

import opentrack.tpg.journey.repository.{ConnectionRepository, StationRepository}
import opentrack.tpg.planner.ConnectionScanAlgorithm
import opentrack.tpg.transferpattern.MinimumSpanningTreeCleaner
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

    val (services, timetable) = connectionRepository.getTimetableConnections(scanDate)
    val nonTimetable = connectionRepository.getNonTimetableConnections(scanDate)
    val mstCleaner = new MinimumSpanningTreeCleaner(services)
    var numDone = 0

//    val csa = new ConnectionScanAlgorithm(timetable, nonTimetable, interchange)
//    val mst = csa.getShortestPathTree("CHX")
//    val cleanedTree = mstCleaner.cleanTree(mst)
//    val mstTBW = mst.get("TBW")
//    val cleanedTBW = cleanedTree.filter(_.journey == "CHXTBW")
//    val futureOptions = List(patternRepository.storeTransferPatterns(cleanedTree))

    val futureOptions =
      for (station <- stations.par) yield {
        val csa = new ConnectionScanAlgorithm(timetable, nonTimetable, interchange)
        val mst = csa.getShortestPathTree(station)
        val cleanedTree = mstCleaner.cleanTree(mst)

        numDone += 1
        println(s"Done $station ($numDone of ${stations.size})")
        patternRepository.storeTransferPatterns(cleanedTree)
      }

    Await.result(Future.sequence(patternRepository.updateLastScanDate(scanDate) :: futureOptions.toList.flatten), 60 seconds)
  }

}
