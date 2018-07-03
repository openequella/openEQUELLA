package com.tle.core.db

import com.tle.core.db.migration.DBSchemaMigration
import com.tle.core.db.types.DbUUID
import io.doolse.simpledba.Iso
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.jdbc.oracle._

object OracleSchema extends DBSchemaMigration with DBSchema with DBQueries with StdOracleColumns {

  implicit lazy val config = setupLogging(oracleConfig)

  lazy val hibSeq = Sequence[Long]("hibernate_sequence")

  def autoIdCol = longCol

  override def insertAuditLog = insertWith(auditLog, hibSeq)

  def dbUuidCol =
    wrap[String, DbUUID](stringCol, _.isoMap(Iso(_.id.toString, DbUUID.fromString)), _.copy(typeName = "VARCHAR(36)"))

}
