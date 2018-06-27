package com.tle.core.db
import com.tle.core.db.migration.DBSchemaMigration
import com.tle.core.db.tables.{AttachmentViewCount, AuditLogEntry, ItemViewCount}
import com.tle.core.db.types.InstId
import io.doolse.simpledba.jdbc.sqlserver._
import io.doolse.simpledba.jdbc.{BinOp, TableColumns, TableMapper}
import shapeless.HList
import shapeless.syntax.singleton._

object SQLServerSchema extends DBSchema with DBQueries with DBSchemaMigration {

  type C[A] = SQLServerColumn[A]
  implicit val config = setupLogging(sqlServerConfig)
  val schemaSQL = config.schemaSQL

  val auditLog = TableMapper[AuditLogEntry].table("audit_log_entry").edit('id, identityCol[Long]).key('id)
  val userAndInst = auditLog.cols(HList('user_id.narrow, 'institution_id.narrow))
  val auditLogQueries = AuditLogQueries(insertIdentity(auditLog),
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

  val itemViewCount = TableMapper[ItemViewCount].table("viewcount_item")
    .keys('inst, 'item_uuid, 'item_version)
  val attachmentViewCount = TableMapper[AttachmentViewCount].table("viewcount_attachment")
    .keys('inst, 'item_uuid, 'item_version, 'attachment)

  val viewCountTables = Seq(itemViewCount.definition, attachmentViewCount.definition)

  val viewCountQueries = ViewCountQueries(itemViewCount.writes, attachmentViewCount.writes,
    itemViewCount.byPK, attachmentViewCount.byPK)

}
