package com.tle.core.db.migration

import com.tle.core.migration.MigrationResult
import io.doolse.simpledba.jdbc.{JDBCIO, TableColumns}


trait DBSchemaMigration {

  def addColumns(columns: TableColumns, progress: MigrationResult): JDBCIO[Unit]

  def auditLogNewColumns: TableColumns

}
