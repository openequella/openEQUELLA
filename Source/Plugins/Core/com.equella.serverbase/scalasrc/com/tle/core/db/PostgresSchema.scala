package com.tle.core.db

import java.util.UUID

import com.tle.core.db.migration.DBSchemaMigration
import com.tle.core.db.types.DbUUID
import io.doolse.simpledba.Iso
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

  def dbUuidCol = wrap[String, DbUUID](stringCol, _.isoMap(Iso(_.id.toString, DbUUID.fromString)),
    _.copy(typeName = "VARCHAR(36)"))

}
