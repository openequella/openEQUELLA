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

package com.tle.core.db.tables

import java.time.Instant

import com.tle.core.db.types._
import io.doolse.simpledba.Iso
import io.circe.generic.auto._

case class AuditLogMeta(referrer: Option[String] = None) extends JsonColumn

object AuditLogMeta {
  implicit def iso: Iso[AuditLogMeta, Option[String]] = JsonColumn.mkCirceIso(AuditLogMeta(None))
}

case class AuditLogEntry(id: Long,
                         data1: Option[String255],
                         data2: Option[String255],
                         data3: Option[String255],
                         data4: Option[String],
                         event_category: String20,
                         event_type: String20,
                         session_id: String40,
                         meta: AuditLogMeta,
                         timestamp: Instant,
                         user_id: UserId,
                         institution_id: InstId)
