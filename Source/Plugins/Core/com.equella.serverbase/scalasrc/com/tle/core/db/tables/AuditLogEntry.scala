package com.tle.core.db.tables

import java.time.Instant

import com.tle.core.db.types._
import io.doolse.simpledba.Iso
import io.circe.generic.auto._

case class AuditLogMeta(referrer: Option[String] = None) extends JsonColumn

object AuditLogMeta
{
  implicit def iso: Iso[AuditLogMeta, Option[String]] = JsonColumn.mkCirceIso(AuditLogMeta(None))
}

case class AuditLogEntry(id: Long, data1: Option[String255], data2: Option[String255], data3: Option[String255],
                         data4: Option[String],
                         event_category: String20, event_type: String20,
                         session_id: String40, meta: AuditLogMeta,
                         timestamp: Instant, user_id: UserId, institution_id: InstId)
