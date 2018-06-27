package com.tle.core.db

import java.util

import com.tle.core.db.migration.DBSchemaMigration
import com.tle.core.hibernate.factory.guice.HibernateFactoryModule
import io.doolse.simpledba.jdbc.{JDBCSchemaSQL, NamedColumn, TableColumns, TableDefinition}

import scala.collection.JavaConverters._

trait DBSchema {

  val schemaSQL : JDBCSchemaSQL

  def indexEach(cols: TableColumns, name: NamedColumn => String): Seq[String] =
    cols.columns.map { cb =>
      schemaSQL.createIndex(TableColumns(cols.name, Seq(cb)), name(cb))
    }

  def auditLogTable : TableDefinition
  def auditLogIndexColumns: TableColumns
  def viewCountTables: Seq[TableDefinition]

  def creationSQL: util.Collection[String] = {
    Seq(schemaSQL.createTable(auditLogTable)) ++
      indexEach(auditLogIndexColumns, "audit_" + _.name) ++
      viewCountTables.map(schemaSQL.createTable)
  }.asJava

}

object DBSchema
{
  lazy private val schemaForDBType: DBSchema with DBQueries with DBSchemaMigration = {
    val p = new HibernateFactoryModule
    p.getProperty("hibernate.connection.driver_class") match {
      case "org.postgresql.Driver" => PostgresSchema
      case "com.microsoft.sqlserver.jdbc.SQLServerDriver" => SQLServerSchema
      case "oracle.jdbc.driver.OracleDriver" => OracleSchema
    }
  }

  def schema : DBSchema = schemaForDBType

  def schemaMigration : DBSchemaMigration = schemaForDBType

  def queries : DBQueries = schemaForDBType
}
