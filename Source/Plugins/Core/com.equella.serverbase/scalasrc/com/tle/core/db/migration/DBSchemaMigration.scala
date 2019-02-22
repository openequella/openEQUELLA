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

import com.tle.core.migration.{MigrationResult, MigrationStatusLog}
import fs2.Stream
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.syntax._

trait DBSchemaMigration {

  type C[A] <: JDBCColumn
  def config: JDBCSQLConfig[C]
  def schemaSQL: JDBCSchemaSQL

  def withSQLLog(progress: MigrationResult): JDBCConfig = {
    config.withPrepareLogger(sql => progress.addLogEntry(new MigrationStatusLog(sql, false)))
  }

  def sqlStmts(progress: MigrationResult, sql: Seq[String]): JDBCIO[Unit] = {
    implicit val c = withSQLLog(progress)
    Stream.emits(sql.map(rawSQL)).covary[JDBCIO].flush.compile.drain
  }

  def addColumns(columns: TableColumns, progress: MigrationResult): JDBCIO[Unit] = {
    progress.setCanRetry(true)
    sqlStmts(progress, schemaSQL.addColumns(columns))
  }

  def addTables(tables: Seq[TableDefinition], progress: MigrationResult): JDBCIO[Unit] = {
    addTablesAndIndexes(tables, Seq.empty, progress)
  }

  def addTablesAndIndexes(tables: Seq[TableDefinition],
                          indexes: Seq[(TableColumns, String)],
                          progress: MigrationResult): JDBCIO[Unit] = {
    progress.setCanRetry(true)
    val sql = tables.map(schemaSQL.createTable) ++
      indexes.map(i => schemaSQL.createIndex(i._1, i._2))
    sqlStmts(progress, sql)
  }

  def addIndexes(indexes: Seq[(TableColumns, String)], progress: MigrationResult): JDBCIO[Unit] = {
    addTablesAndIndexes(Seq.empty, indexes, progress)
  }

  def auditLogNewColumns: TableColumns
  def viewCountTables: Seq[TableDefinition]

  def newEntityTables: Seq[TableDefinition]
  def newEntityIndexes: Seq[(TableColumns, String)]
}
