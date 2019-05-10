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
import com.tle.core.db.migration.DBSchemaMigration
import com.tle.core.db.types.DbUUID
import io.doolse.simpledba.Iso
import io.doolse.simpledba.jdbc.StandardJDBC
import io.doolse.simpledba.jdbc.sqlserver._

object SQLServerSchema
    extends DBSchema
    with DBQueries
    with DBSchemaMigration
    with StdSQLServerColumns {

  implicit lazy val config = {
    val escaped = StandardJDBC.escapeReserved(StandardJDBC.DefaultReserved + "key") _
    setupLogging(sqlServerConfig.copy(escapeColumnName = escaped))
  }
  override def autoIdCol: SQLServerColumn[Long] = identityCol[Long]

  override def insertAuditLog = insertIdentity(auditLog)

  override def insertCachedValue = insertIdentity(cachedValues)

  def dbUuidCol =
    wrap[String, DbUUID](stringCol,
                         _.isoMap(Iso(_.id.toString, DbUUID.fromString)),
                         _.copy(typeName = "VARCHAR(36)"))
}
