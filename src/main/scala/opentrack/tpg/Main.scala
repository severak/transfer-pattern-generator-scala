package opentrack.tpg

import com.github.mauricio.async.db.Configuration
import com.github.mauricio.async.db.mysql.MySQLConnection
import opentrack.tpg.journey.repository.{ConnectionRepository, StationRepository}
import opentrack.tpg.transferpattern.command.FindTransferPatterns
import opentrack.tpg.transferpattern.repository.TransferPatternRepository
import com.github.mauricio.async.db.mysql.pool.MySQLConnectionFactory
import com.github.mauricio.async.db.pool.{ConnectionPool, PoolConfiguration}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by linus on 30/09/16.
  */
object Main extends App {
  val config = new Configuration("root", "localhost", 3306, None, Some("ojp"))
  val factory = new MySQLConnectionFactory(config)
  val db = new ConnectionPool[MySQLConnection](factory, PoolConfiguration.Default)

  Await.result(db.connect, 5 seconds)

  FindTransferPatterns(
    new TransferPatternRepository(db),
    new StationRepository(db),
    new ConnectionRepository(db)
  )

  db.close
}
