package opentrack.tpg

import com.zaxxer.hikari.HikariDataSource
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
  val dataSource = {
    val ds = new HikariDataSource()
    ds.setDriverClassName("com.mysql.jdbc.Driver")
    ds.setJdbcUrl("jdbc:mysql://localhost/ojp")
    ds.addDataSourceProperty("user", "root")
    ds.addDataSourceProperty("maximumPoolSize", 20)
    ds
  }

  ConnectionPool.singleton(new DataSourceConnectionPool(dataSource))

  FindTransferPatterns(
    new TransferPatternRepository(),
    new StationRepository(),
    new ConnectionRepository()
  )

  dataSource.close()
}
