package com.tle.core.db

import java.util

import com.tle.core.hibernate.factory.guice.HibernateFactoryModule
import io.doolse.simpledba.jdbc.{ColumnBinding, JDBCSchemaSQL, TableColumns, TableDefinition}

import scala.collection.JavaConverters._

trait DBSchema {

  val schemaSQL : JDBCSchemaSQL

  def indexEach(cols: TableColumns, name: ColumnBinding => String): Seq[String] =
    cols.columns.map { cb =>
      schemaSQL.createIndex(TableColumns(cols.name, Seq(cb)), name(cb))
    }

  val auditLogTable : TableDefinition
  val auditLogIndexColumns: TableColumns

  def creationSQL: util.Collection[String] = {
    (Seq(schemaSQL.createTable(auditLogTable)) ++
      indexEach(auditLogIndexColumns, "audit_" + _.name))
      .asJava
  }

}

object DBSchema
{
  lazy val schemaForDBType: DBSchema = {
    val p = new HibernateFactoryModule
    p.getProperty("hibernate.connection.driver_class") match {
      case "org.postgresql.Driver" => PostgresSchema
      case "com.microsoft.sqlserver.jdbc.SQLServerDriver" => SQLServerSchema
      case "oracle.jdbc.driver.OracleDriver" => ???
    }
  }
}
