package opentrack.tpg.journey.query

import java.time.LocalDate

import opentrack.tpg.journey.repository.{ConnectionRepository, StationRepository}
import opentrack.tpg.planner.ConnectionScanAlgorithm

/**
  * Created by linus on 23/11/16.
  */
object EarliestArrivalQuery {

  def apply(stationRepository: StationRepository, connectionRepository: ConnectionRepository): Unit = {
    val timetable = connectionRepository.getTimetableConnections(LocalDate.now())
    val nonTimetable = connectionRepository.getNonTimetableConnections(LocalDate.now())
    val interchange = stationRepository.interchange
    val csa = new ConnectionScanAlgorithm(timetable, nonTimetable, interchange)

    println(csa.getJourney("BGA", "NRW", 900))
  }
}
