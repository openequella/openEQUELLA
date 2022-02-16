/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.db

import com.tle.core.db.tables._
import com.tle.core.db.types.{DbUUID, JsonColumn}
import com.tle.core.hibernate.factory.guice.HibernateFactoryModule
import io.circe.Json
import io.doolse.simpledba._
import io.doolse.simpledba.jdbc._
import scala.collection.mutable

trait DBSchema extends StdColumns {
  // General note on the queries in this class - With the advent of hibernate 5,
  // queries with '?' in them need to be ordinal ( ie `?4` ).  However, this class
  // does not leverage the JPA / Hibernate logic, so we can leave the `?`s as-is.

  implicit def config: JDBCConfig.Aux[C]

  implicit def dbUuidCol: C[DbUUID]

  def schemaSQL: JDBCSchemaSQL = config.schemaSQL

  val allTables: mutable.Buffer[TableDefinition]         = mutable.Buffer()
  val allIndexes: mutable.Buffer[(TableColumns, String)] = mutable.Buffer()

  def indexEach(cols: TableColumns, name: NamedColumn => String): Seq[(TableColumns, String)] =
    cols.columns.map { cb =>
      TableColumns(cols.name, Seq(cb)) -> name(cb)
    }

  implicit def dbJsonCol(implicit scol: C[String]): C[Json] =
    wrap[String, Json](scol, _.isoMap(JsonColumn.jsonStringIso), jsonColumnMod)

  def jsonColumnMod(ct: ColumnType): ColumnType = ct

  implicit def jsonColumns[A <: JsonColumn](
      implicit c: Iso[A, Option[String]],
      col: C[Option[String]]
  ): C[A] =
    wrap[Option[String], A](col, _.isoMap[A](c), jsonColumnMod)

  def autoIdCol: C[Long]

  val userAndInst = Cols('user_id, 'institution_id)
  val itemViewId  = Cols('inst, 'item_uuid, 'item_version)

  val settingsRel =
    TableMapper[Setting].table("configuration_property").keys(Cols('institution_id, 'property))

  val settingsQueries = SettingsQueries(
    settingsRel.writes,
    settingsRel.byPK,
    settingsRel.query
      .where(Cols('institution_id), BinOp.EQ)
      .where(Cols('property), BinOp.LIKE)
      .build,
    settingsRel.query
      .where(Cols('property), BinOp.LIKE)
      .build
  )

  val entityTable = TableMapper[OEQEntity].table("entities").keys(Cols('inst_id, 'uuid))

  val entityQueries = EntityQueries(
    entityTable.writes,
    entityTable.query.where(Cols('inst_id, 'typeid), BinOp.EQ).build,
    entityTable.byPK,
    entityTable.query.where(Cols('inst_id), BinOp.EQ).build
  )
}

object DBSchema {
  lazy private val schemaForDBType: DBSchema with DBQueries = {
    val p = new HibernateFactoryModule
    p.getProperty("hibernate.connection.driver_class") match {
      case "org.postgresql.Driver"                        => PostgresSchema
      case "com.microsoft.sqlserver.jdbc.SQLServerDriver" => SQLServerSchema
      case "oracle.jdbc.driver.OracleDriver"              => OracleSchema
    }
  }

  def schema: DBSchema = schemaForDBType

  def queries: DBQueries = schemaForDBType
}
