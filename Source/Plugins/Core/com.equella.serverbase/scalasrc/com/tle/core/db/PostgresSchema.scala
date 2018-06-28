package com.tle.core.db

import com.tle.core.db.migration.DBSchemaMigration
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.jdbc.postgres._

object PostgresSchema extends DBSchemaMigration with DBSchema with DBQueries with StdPostgresColumns {

  implicit def config = setupLogging(postgresConfig)

  override def jsonColumnMod(ct: ColumnType): ColumnType = ct.copy(typeName = "JSONB")

  lazy val hibSeq = Sequence[Long]("hibernate_sequence")

  def autoIdCol = longCol

  override def insertAuditLog = {
    insertWith(auditLog, hibSeq)
  }


}
