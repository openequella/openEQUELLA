package com.tle.core.db

import java.util

import com.tle.core.db.migration.DBSchemaMigration
import com.tle.core.db.tables.{AttachmentViewCount, AuditLogEntry, ItemViewCount}
import com.tle.core.db.types.{DbUUID, InstId, JsonColumn}
import com.tle.core.hibernate.factory.guice.HibernateFactoryModule
import fs2.Stream
import io.doolse.simpledba.Iso
import io.doolse.simpledba.jdbc._
import shapeless.HList
import shapeless.syntax.singleton._

import scala.collection.JavaConverters._

trait DBSchema extends StdColumns {

  implicit def config: JDBCConfig.Aux[C]

  implicit def dbUuidCol: C[DbUUID]

  def schemaSQL : JDBCSchemaSQL = config.schemaSQL

  def indexEach(cols: TableColumns, name: NamedColumn => String): Seq[String] =
    cols.columns.map { cb =>
      schemaSQL.createIndex(TableColumns(cols.name, Seq(cb)), name(cb))
    }


  def jsonColumnMod(ct: ColumnType): ColumnType = ct

  implicit def jsonColumns[A <: JsonColumn](implicit c: Iso[A, Option[String]], col: C[Option[String]]): C[A] =
    wrap[Option[String], A](col, _.isoMap[A](c), jsonColumnMod)

  def autoIdCol: C[Long]

  val auditLog = TableMapper[AuditLogEntry].table("audit_log_entry").edit('id, autoIdCol).key('id)

  def insertAuditLog: (Long => AuditLogEntry) => Stream[JDBCIO, AuditLogEntry]

  val userAndInst = auditLog.cols(HList('user_id.narrow, 'institution_id.narrow))

  val auditLogQueries = AuditLogQueries(insertAuditLog,
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

  def creationSQL: util.Collection[String] = {
    Seq(schemaSQL.createTable(auditLogTable)) ++
      indexEach(auditLogIndexColumns, "audit_" + _.name) ++
      viewCountTables.map(schemaSQL.createTable)
  }.asJava



}

object DBSchema
{
  lazy private val schemaForDBType: DBSchema with DBQueries with DBSchemaMigration = {
    val p = new HibernateFactoryModule
    p.getProperty("hibernate.connection.driver_class") match {
      case "org.postgresql.Driver" => PostgresSchema
      case "com.microsoft.sqlserver.jdbc.SQLServerDriver" => SQLServerSchema
      case "oracle.jdbc.driver.OracleDriver" => OracleSchema
    }
  }

  def schema : DBSchema = schemaForDBType

  def schemaMigration : DBSchemaMigration = schemaForDBType

  def queries : DBQueries = schemaForDBType
}
