package com.tle.core.db

import com.tle.core.db.migration.DBSchemaMigration
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.jdbc.oracle._

object OracleSchema extends DBSchemaMigration with DBSchema with DBQueries with StdOracleColumns {

  implicit val config = setupLogging(oracleConfig)

  val hibSeq = Sequence[Long]("hibernate_sequence")

  def autoIdCol = longCol

  override def insertAuditLog = insertWith(auditLog, hibSeq)

}
