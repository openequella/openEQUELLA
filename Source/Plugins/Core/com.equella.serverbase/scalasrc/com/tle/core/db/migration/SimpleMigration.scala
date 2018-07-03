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

import java.util
import java.util.{Calendar, Collections}

import com.tle.common.i18n.StringLookup
import com.tle.core.db.{DBSchema, RunWithDB}
import com.tle.core.hibernate.CurrentDataSource
import com.tle.core.migration.{Migration, MigrationExt, MigrationInfo, MigrationResult}
import io.doolse.simpledba.jdbc.JDBCIO

abstract class SimpleMigration(val id: String, year: Int, month: Int, day: Int, strings: StringLookup) extends MigrationExt {

  def migration(progress: MigrationResult, schema: DBSchemaMigration): JDBCIO[Unit]

  override def placeholder(): Boolean = false

  override def migration(): Migration = new Migration {
    override def isBackwardsCompatible: Boolean = true

    override def migrate(status: MigrationResult): Unit = {
      val mig = migration(status, DBSchema.schemaMigration)
      RunWithDB.executeTransaction(CurrentDataSource.get().getDataSource.getConnection, mig)
    }

    override def createMigrationInfo(): MigrationInfo =
      new MigrationInfo(strings.key("title"), strings.key("desc"))
  }

  override def initial(): Boolean = false

  override def system(): Boolean = false

  override def getObsoletedBy: util.Set[String] = Collections.emptySet()

  override def getFixes: util.Set[String] = Collections.emptySet()

  override def getIfSkipped: util.Set[String] = Collections.emptySet()

  override def getDepends: util.Set[String] = Collections.emptySet()

  val date = {
    val c = Calendar.getInstance()
    c.set(year, month, day)
    c.getTime
  }
}
