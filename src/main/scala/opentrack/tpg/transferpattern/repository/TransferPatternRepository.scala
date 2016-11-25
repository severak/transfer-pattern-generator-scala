package opentrack.tpg.transferpattern.repository

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import opentrack.tpg.journey.{MST, Station}
import redis.RedisClient

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * Created by linus on 08/10/16.
  */
class TransferPatternRepository(redis: RedisClient)(implicit val context: ExecutionContext) {

  def nextScanDate = {
    redis.get("last_scan").map {
      case Some(result) => LocalDate.parse(result.utf8String, DateTimeFormatter.ISO_LOCAL_DATE).plusDays(1)
      case None => LocalDate.now().plusDays(1)
    }
  }

  def updateLastScanDate(date: LocalDate) = {
    redis.set("last_scan", date.format(DateTimeFormatter.ISO_LOCAL_DATE))
  }

  def storeTransferPatterns(station: Station, patterns: MST) = {
    val tx = redis.transaction()

    for (
      (station, journeys) <- patterns;
      (time, journey) <- journeys if journey.legs.length < 10
    ) yield tx.sadd(journey.origin + journey.destination, journey.hash)

    Await.result(tx.exec(), 60 seconds)
  }

}
