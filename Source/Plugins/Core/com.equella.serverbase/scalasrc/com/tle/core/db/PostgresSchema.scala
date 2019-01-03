/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.core.db

import java.util.UUID

import com.tle.core.db.migration.DBSchemaMigration
import com.tle.core.db.types.DbUUID
import io.doolse.simpledba.Iso
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.jdbc.postgres._

object PostgresSchema extends DBSchemaMigration with DBSchema with DBQueries with StdPostgresColumns {

  implicit def config = setupLogging(postgresConfig)

  override def jsonColumnMod(ct: ColumnType): ColumnType = ct.copy(typeName = "JSONB")

  lazy val hibSeq = Sequence[Long]("hibernate_sequence")

  def autoIdCol = longCol

  override def insertAuditLog = {
    insertWith(auditLog, hibSeq)
  }

  def dbUuidCol = wrap[String, DbUUID](stringCol, _.isoMap(Iso(_.id.toString, DbUUID.fromString)),
    _.copy(typeName = "VARCHAR(36)"))

}
