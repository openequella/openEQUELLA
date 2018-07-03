package com.tle.core.db.migration

import com.tle.core.migration.{MigrationResult, MigrationStatusLog}
import fs2.Stream
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.syntax._

trait DBSchemaMigration {

  type C[A] <: JDBCColumn
  def config : JDBCSQLConfig[C]
  def schemaSQL : JDBCSchemaSQL

  def withSQLLog(progress: MigrationResult): JDBCConfig = {
    config.withPrepareLogger(sql => progress.addLogEntry(new MigrationStatusLog(sql, false)))
  }

  def addColumns(columns: TableColumns, progress: MigrationResult): JDBCIO[Unit] = {
    progress.setCanRetry(true)
    implicit val c = withSQLLog(progress)
    Stream.emits(schemaSQL.addColumns(columns).map(rawSQL)).covary[JDBCIO].flush.compile.drain
  }

  def addTables(tables: Seq[TableDefinition], progress: MigrationResult): JDBCIO[Unit] = {
    progress.setCanRetry(true)
    implicit val c = withSQLLog(progress)
    Stream.emits(tables.map(schemaSQL.createTable).map(rawSQL)).covary[JDBCIO].flush.compile.drain
  }

  def auditLogNewColumns: TableColumns
  def viewCountTables: Seq[TableDefinition]

}
