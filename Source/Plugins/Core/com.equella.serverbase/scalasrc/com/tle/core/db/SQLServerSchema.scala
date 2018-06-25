package com.tle.core.db
import com.tle.core.db.tables.AuditLogEntry
import com.tle.core.db.types.JsonColumn
import io.doolse.simpledba.Iso
import io.doolse.simpledba.jdbc.sqlserver._
import io.doolse.simpledba.jdbc.{EQ, StdJDBCColumn, TableColumns, TableMapper}
import shapeless.HList
import shapeless.syntax.singleton._

object SQLServerSchema extends DBSchema with DBQueries {
  implicit val config = sqlServerConfig.withPrepareLogger(System.err.println)
  val schemaSQL = config.schemaSQL

  implicit def jsonColumns[A <: JsonColumn](implicit c: Iso[A, Option[String]]): SQLServerColumn[A] = {
    SQLServerColumn(StdJDBCColumn.optionalColumn(StdJDBCColumn.stringCol).isoMap(c), SQLServerColumn.stringCol.columnType)
  }

  val auditLog = TableMapper[AuditLogEntry].table("audit_log_entry").edit('id, identityCol[Long]).key('id)
  val userAndInst = auditLog.cols(HList('user_id.narrow, 'institution_id.narrow))
  val auditLogQueries = AuditLogQueries(insertIdentity(auditLog),
    auditLog.delete.where(userAndInst, EQ).build,
    auditLog.query.whereEQ(userAndInst).build)
  val auditLogTable = auditLog.definition

  val auditLogIndexColumns : TableColumns = auditLog.subsetOf('institution_id, 'timestamp, 'event_category, 'event_type,
    'user_id, 'session_id, 'data1, 'data2, 'data3)

  val auditLogNewColumns = auditLog.subset('meta)
}
