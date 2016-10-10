package opentrack.tpg

import opentrack.tpg.journey.repository.{ConnectionRepository, StationRepository}
import opentrack.tpg.transferpattern.command.FindTransferPatterns
import opentrack.tpg.transferpattern.repository.TransferPatternRepository
import scalikejdbc._

/**
  * Created by linus on 30/09/16.
  */
object Main extends App {

  ConnectionPool.singleton("jdbc:mysql://localhost/ojp", "root", null)
  using(DB(ConnectionPool.borrow())) { db =>
    val cmd = new FindTransferPatterns(
      new TransferPatternRepository(db),
      new StationRepository(db),
      new ConnectionRepository(db)
    )

    cmd.run()
  }
}
