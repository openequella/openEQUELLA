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

import java.time.Instant

import com.tle.core.db.tables._
import com.tle.core.db.types.{DbUUID, InstId, String20, String255, UserId}
import fs2.Stream
import io.doolse.simpledba.{WriteOp, WriteQueries}
import io.doolse.simpledba.jdbc.{JDBCColumn, JDBCIO, JDBCSQLConfig}
import org.slf4j.LoggerFactory

case class AuditLogQueries(
    insertNew: (Long => AuditLogEntry) => Stream[JDBCIO, AuditLogEntry],
    deleteForUser: ((UserId, InstId)) => Stream[JDBCIO, WriteOp],
    listForUser: ((UserId, InstId)) => Stream[JDBCIO, AuditLogEntry],
    deleteForInst: InstId => Stream[JDBCIO, WriteOp],
    deleteBefore: Instant => Stream[JDBCIO, WriteOp],
    countForInst: InstId => Stream[JDBCIO, Int],
    listForInst: InstId => Stream[JDBCIO, AuditLogEntry]
)

case class ViewCountQueries(
    writeItemCounts: WriteQueries[JDBCIO, ItemViewCount],
    writeAttachmentCounts: WriteQueries[JDBCIO, AttachmentViewCount],
    itemCount: ((InstId, DbUUID, Int)) => Stream[JDBCIO, ItemViewCount],
    allItemCount: InstId => Stream[JDBCIO, ItemViewCount],
    attachmentCount: (
        (InstId, DbUUID, Int, DbUUID)
    ) => Stream[JDBCIO, AttachmentViewCount],
    allAttachmentCount: (
        (InstId, DbUUID, Int)
    ) => Stream[JDBCIO, AttachmentViewCount],
    countForCollectionId: Long => Stream[JDBCIO, Int],
    attachmentCountForCollectionId: Long => Stream[JDBCIO, Int],
    deleteForItemId: ((InstId, DbUUID, Int)) => Stream[JDBCIO, WriteOp]
)

case class SettingsQueries(
    write: WriteQueries[JDBCIO, Setting],
    query: ((InstId, String)) => Stream[JDBCIO, Setting],
    prefixQuery: ((InstId, String)) => Stream[JDBCIO, Setting],
    prefixAnyInst: String => Stream[JDBCIO, Setting]
)

case class EntityQueries(
    write: WriteQueries[JDBCIO, OEQEntity],
    allByType: ((InstId, String20)) => Stream[JDBCIO, OEQEntity],
    byId: ((InstId, DbUUID)) => Stream[JDBCIO, OEQEntity],
    allByInst: InstId => Stream[JDBCIO, OEQEntity]
)

case class CachedValueQueries(
    insertNew: (Long => CachedValue) => Stream[JDBCIO, CachedValue],
    writes: WriteQueries[JDBCIO, CachedValue],
    getForKey: ((String255, String255, InstId)) => Stream[JDBCIO, CachedValue],
    getForValue: ((String255, String, InstId)) => Stream[JDBCIO, CachedValue])

object DBQueries {
  val logSQL = LoggerFactory.getLogger("org.hibernate.SQL")
}

trait DBQueries {

  type C[A] <: JDBCColumn

  def setupLogging(config: JDBCSQLConfig[C]): JDBCSQLConfig[C] =
    config.withPrepareLogger(sql => DBQueries.logSQL.debug(sql))

  def auditLogQueries: AuditLogQueries

  def viewCountQueries: ViewCountQueries

  def settingsQueries: SettingsQueries

  def entityQueries: EntityQueries

  def cachedValueQueries: CachedValueQueries
}
