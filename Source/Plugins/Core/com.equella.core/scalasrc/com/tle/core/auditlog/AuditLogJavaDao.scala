package com.tle.core.auditlog

import java.time.Instant
import java.util.Date

import cats.data.Kleisli
import com.tle.beans.Institution
import com.tle.core.db.tables.{AuditLogEntry, AuditLogMeta}
import com.tle.core.db.types.UserId
import com.tle.core.db.{DBSchema, RunWithDB}

object AuditLogJavaDao {

  val queries = DBSchema.queries.auditLogQueries

  def removeEntriesForInstitution(institution: Institution): Unit = ???

  def removeEntriesBeforeDate(date: Date): Unit = ???

  def countForInstitution(institution: Institution): Long = ???

  def listEntries(offset: Int, maximum: Int, institution: Institution): java.util.List[AuditLogEntry] = ???

  def log(userId: String, sessionId: String, category: String,
          `type`: String, d1: String, d2: String, d3: String,
          d4: String, institution: Institution): Unit = {
    RunWithDB.execute( Kleisli {
      _ => queries.insertNew(id => AuditLogEntry(id, d1, d2, d3, Option(d4),
        category, `type`, sessionId, AuditLogMeta(), Instant.now(),
        UserId(userId), institution.getDatabaseId)).compile.drain
    })
  }
}
