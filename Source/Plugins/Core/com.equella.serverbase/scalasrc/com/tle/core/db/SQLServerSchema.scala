package com.tle.core.db
import com.tle.core.db.tables.AuditLogEntry
import com.tle.core.db.types.JsonColumn
import io.doolse.simpledba.Iso
import io.doolse.simpledba.jdbc.sqlserver._
import io.doolse.simpledba.jdbc.{JDBCSchemaSQL, StdJDBCColumn, TableColumns, TableDefinition, TableMapper}

object SQLServerSchema extends DBSchema  {
  implicit val config = sqlServerConfig
  val schemaSQL = config.schemaSQL

  implicit def jsonColumns[A <: JsonColumn](implicit c: Iso[A, Option[String]]): SQLServerColumn[A] = {
    SQLServerColumn(StdJDBCColumn.optionalColumn(StdJDBCColumn.stringCol).isoMap(c))
  }

  val auditLog = TableMapper[AuditLogEntry].table("audit_log_entry").key('id)
  val auditLogTable = auditLog.definition

  val auditLogIndexColumns : TableColumns = auditLog.subsetOf('institution_id, 'timestamp, 'event_category, 'event_type,
    'user_id, 'session_id, 'data1, 'data2, 'data3)

  val auditLogNewColumns = auditLog.subset('meta)
}
