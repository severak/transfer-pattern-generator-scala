package opentrack.tpg

import opentrack.tpg.journey.repository.{ConnectionRepository, StationRepository}
import opentrack.tpg.transferpattern.command.FindTransferPatterns
import opentrack.tpg.transferpattern.repository.TransferPatternRepository
import scalikejdbc._
import scalikejdbc.config._

/**
  * Created by linus on 30/09/16.
  */
object Main extends App {

  DBs.setupAll()

  val cmd = new FindTransferPatterns(
    new TransferPatternRepository(),
    new StationRepository(),
    new ConnectionRepository()
  )

  cmd.run()

}
