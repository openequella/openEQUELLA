/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.db.migration

import com.tle.core.i18n.ServerStrings
import com.tle.core.migration.MigrationResult
import io.doolse.simpledba.jdbc.JDBCIO

object NewAuditLogColumn
    extends SimpleMigration("NewAuditLogColumn",
                            2018,
                            6,
                            1,
                            ServerStrings.lookup.prefix("mig.auditcol")) {
  def migration(progress: MigrationResult, schemaMigration: DBSchemaMigration): JDBCIO[Unit] = {
    schemaMigration.addColumns(schemaMigration.auditLogNewColumns, progress)
  }
}

object NewViewCountTables
    extends SimpleMigration("NewViewCountTables",
                            2018,
                            6,
                            26,
                            ServerStrings.lookup.prefix("mig.viewcount")) {
  def migration(progress: MigrationResult, schemaMigration: DBSchemaMigration): JDBCIO[Unit] = {
    schemaMigration.addTables(schemaMigration.viewCountTables, progress)
  }
}
