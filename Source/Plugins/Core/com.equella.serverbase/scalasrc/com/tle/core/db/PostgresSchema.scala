package com.tle.core.db

import com.tle.core.db.migration.DBSchemaMigration
import com.tle.core.db.tables.AuditLogEntry
import com.tle.core.db.types.JsonColumn
import com.tle.core.migration.MigrationResult
import fs2.Stream
import io.doolse.simpledba.Iso
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.jdbc.postgres._
import io.doolse.simpledba.syntax._

object PostgresSchema extends DBSchemaMigration with DBSchema {

  implicit val config = postgresConfig
  val schemaSQL = config.schemaSQL

  implicit def jsonColumns[A <: JsonColumn](implicit c: Iso[A, Option[String]]): PostgresColumn[A] = {
    PostgresColumn(StdJDBCColumn.optionalColumn(StdJDBCColumn.stringCol).copy(sqlType = JSONBType).isoMap(c))
  }

  val auditLog = TableMapper[AuditLogEntry].table("audit_log_entry").key('id)
  val auditLogTable = auditLog.definition

  val auditLogIndexColumns : TableColumns = auditLog.subsetOf('institution_id, 'timestamp, 'event_category, 'event_type,
    'user_id, 'session_id, 'data1, 'data2, 'data3)

  val auditLogNewColumns = auditLog.subset('meta)

  def addColumns(columns: TableColumns, progress: MigrationResult): JDBCIO[Unit] = {
    progress.setCanRetry(true)
    Stream.emits(schemaSQL.addColumns(columns).map(rawSQL)).covary[JDBCIO].flush.compile.drain
  }
}
