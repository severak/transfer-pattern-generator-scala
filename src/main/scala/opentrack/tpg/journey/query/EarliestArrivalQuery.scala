package opentrack.tpg.journey.query

import java.time.LocalDate

import opentrack.tpg.journey.repository.{ConnectionRepository, StationRepository}
import opentrack.tpg.planner.ConnectionScanAlgorithm

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by linus on 23/11/16.
  */
object EarliestArrivalQuery {

  def apply(stationRepository: StationRepository, connectionRepository: ConnectionRepository): Unit = {
    val timetable = connectionRepository.getTimetableConnections(LocalDate.now())
    val nonTimetable = connectionRepository.getNonTimetableConnections(LocalDate.now())
    val interchange = Await.result(stationRepository.interchange, 5 seconds)
    val csa = new ConnectionScanAlgorithm(timetable._2, nonTimetable, interchange)

    println(csa.getJourney("BGA", "NRW", 900))
  }
}
