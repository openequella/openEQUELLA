package com.tle.core.db
import java.util.UUID

import com.tle.core.db.migration.DBSchemaMigration
import com.tle.core.db.types.DbUUID
import io.doolse.simpledba.Iso
import io.doolse.simpledba.jdbc.sqlserver._

object SQLServerSchema extends DBSchema with DBQueries with DBSchemaMigration with StdSQLServerColumns {

  implicit def config = setupLogging(sqlServerConfig)

  override def autoIdCol: SQLServerColumn[Long] = identityCol[Long]

  override def insertAuditLog = insertIdentity(auditLog)

  def dbUuidCol = wrap[String, DbUUID](stringCol, _.isoMap(Iso(_.id.toString, DbUUID.fromString)),
    _.copy(typeName = "VARCHAR(36)"))
}
