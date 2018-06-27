package com.tle.core.db

import com.tle.core.db.migration.DBSchemaMigration
import com.tle.core.db.tables.{AttachmentViewCount, AuditLogEntry, ItemViewCount}
import com.tle.core.db.types.{InstId, JsonColumn}
import io.doolse.simpledba.Iso
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.jdbc.postgres._
import shapeless._
import shapeless.syntax.singleton._

object PostgresSchema extends DBSchemaMigration with DBSchema with DBQueries {

  type C[A] = PostgresColumn[A]
  implicit val config = setupLogging(postgresConfig)
  val schemaSQL = config.schemaSQL

  val hibSeq = Sequence[Long]("hibernate_sequence")

  implicit def jsonColumns[A <: JsonColumn](implicit c: Iso[A, Option[String]]): PostgresColumn[A] = {
    PostgresColumn(StdJDBCColumn.optionalColumn(StdJDBCColumn.stringCol).isoMap(c), ColumnType("JSONB", nullable = true))
  }

  val auditLog = TableMapper[AuditLogEntry].table("audit_log_entry").key('id)
  val userAndInst = auditLog.cols(HList('user_id.narrow, 'institution_id.narrow))
  val auditLogQueries = AuditLogQueries(insertWith(auditLog, hibSeq),
    auditLog.delete.where(userAndInst, BinOp.EQ).build,
    auditLog.query.where(userAndInst, BinOp.EQ).build,
    auditLog.delete.where('institution_id, BinOp.EQ).build,
    auditLog.delete.where('timestamp, BinOp.LT).build,
    auditLog.select.count.where('institution_id, BinOp.EQ).buildAs[InstId, Int],
    auditLog.query.where('institution_id, BinOp.EQ).build
  )

  val auditLogTable = auditLog.definition

  val auditLogIndexColumns : TableColumns = auditLog.subsetOf('institution_id, 'timestamp, 'event_category, 'event_type,
    'user_id, 'session_id, 'data1, 'data2, 'data3)

  val auditLogNewColumns = auditLog.subset('meta)

  val itemViewCount = TableMapper[ItemViewCount].table("viewcount_item").keys('inst, 'item_uuid, 'item_version)
  val attachmentViewCount = TableMapper[AttachmentViewCount].table("viewcount_attachment").keys('inst, 'item_uuid, 'item_version, 'attachment)

  val viewCountTables = Seq(itemViewCount.definition, attachmentViewCount.definition)

  val viewCountQueries = ViewCountQueries(itemViewCount.writes, attachmentViewCount.writes,
    itemViewCount.byPK, attachmentViewCount.byPK)

}
