package com.tle.core.db
import com.tle.core.db.migration.DBSchemaMigration
import io.doolse.simpledba.jdbc.sqlserver._

object SQLServerSchema extends DBSchema with DBQueries with DBSchemaMigration with StdSQLServerColumns {

  implicit def config = setupLogging(sqlServerConfig)

  override def autoIdCol: SQLServerColumn[Long] = identityCol[Long]

  override def insertAuditLog = insertIdentity(auditLog)

}
