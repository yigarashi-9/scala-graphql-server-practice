package server

import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import slick.jdbc.MySQLProfile.api._

object DB {
  val dbConfig = ConfigFactory.load().getConfig("slick.dbs.default")
  val hikariConfig = new HikariConfig()

  hikariConfig.setDriverClassName(dbConfig.getString("db.driver"))
  hikariConfig.setJdbcUrl(dbConfig.getString("db.url"))
  hikariConfig.setUsername(dbConfig.getString("db.user"))
  hikariConfig.setPassword(dbConfig.getString("db.password"))

  hikariConfig.setMaximumPoolSize(dbConfig.getInt("db.maxConnections"))

  val dataSource = new HikariDataSource(hikariConfig)

  val db = Database.forDataSource(dataSource, None)

  def runSyncRawSQL[R](a: DBIOAction[R, NoStream, Nothing]): R = Await.result(db.run(a), Duration.Inf)
  def generateUUID(): Long = runSyncRawSQL(sql"""SELECT UUID_SHORT()""".as[Long].head)
}
