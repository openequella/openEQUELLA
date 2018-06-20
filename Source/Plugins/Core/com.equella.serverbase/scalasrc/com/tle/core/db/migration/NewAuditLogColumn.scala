package com.tle.core.db.migration

import com.tle.core.db.PostgresSchema
import com.tle.core.i18n.ServerStrings
import com.tle.core.migration.MigrationResult
import io.doolse.simpledba.jdbc.JDBCIO

object NewAuditLogColumn extends SimpleMigration("NewAuditLogColumn", 2018, 6, 1,
  ServerStrings.lookup.prefix("newColumn")) {
  override def migration(progress: MigrationResult): JDBCIO[Unit] =
    PostgresSchema.addColumns(PostgresSchema.auditLogNewColumns, progress)
}
