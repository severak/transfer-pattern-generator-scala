package opentrack.tpg

import opentrack.tpg.journey.repository.{ConnectionRepository, StationRepository}
import opentrack.tpg.transferpattern.command.FindTransferPatterns
import opentrack.tpg.transferpattern.repository.TransferPatternRepository
import redis.RedisClient
import scalikejdbc.config._
import scala.concurrent.Await
import scala.concurrent.duration._


/**
  * Created by linus on 30/09/16.
  */
object Main extends App {

  DBs.setupAll()

  implicit val system = akka.actor.ActorSystem()
  implicit val ec = system.dispatcher

  val redis = RedisClient()
  val futurePong = redis.ping()

  Await.result(futurePong, 5 seconds)
  println(s"Redis connected")

  FindTransferPatterns(
    new TransferPatternRepository(redis),
    new StationRepository(),
    new ConnectionRepository()
  )

  DBs.closeAll()
  system.shutdown()
}
