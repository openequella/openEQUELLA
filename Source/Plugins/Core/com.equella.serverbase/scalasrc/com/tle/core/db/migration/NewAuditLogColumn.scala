package com.tle.core.db.migration

import com.tle.core.i18n.ServerStrings
import com.tle.core.migration.MigrationResult
import io.doolse.simpledba.jdbc.JDBCIO

object NewAuditLogColumn extends SimpleMigration("NewAuditLogColumn", 2018, 6, 1,
  ServerStrings.lookup.prefix("mig.auditcol")) {
  def migration(progress: MigrationResult, schemaMigration: DBSchemaMigration): JDBCIO[Unit] = {
    schemaMigration.addColumns(schemaMigration.auditLogNewColumns, progress)
  }
}

object NewViewCountTables extends SimpleMigration("NewViewCountTables", 2018, 6, 26,
  ServerStrings.lookup.prefix("mig.viewcount"))
{
  def migration(progress: MigrationResult, schemaMigration: DBSchemaMigration): JDBCIO[Unit] = {
    schemaMigration.addTables(schemaMigration.viewCountTables, progress)
  }
}