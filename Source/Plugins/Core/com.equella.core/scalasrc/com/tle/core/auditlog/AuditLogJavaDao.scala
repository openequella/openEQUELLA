package com.tle.core.auditlog

import java.time.Instant
import java.util
import java.util.Date

import cats.data.Kleisli
import com.thoughtworks.xstream.XStream
import com.tle.beans.Institution
import com.tle.common.filesystem.handle.SubTemporaryFile
import com.tle.core.auditlog.convert.AuditLogEntryXml
import com.tle.core.db.tables.{AuditLogEntry, AuditLogMeta}
import com.tle.core.db.types.UserId
import com.tle.core.db.{DB, DBSchema, RunWithDB}
import com.tle.core.institution.convert.{DefaultMessageCallback, XmlHelper}
import javax.servlet.http.HttpServletRequest
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.syntax._
import fs2.Stream
import io.doolse.simpledba.WriteOp

import scala.collection.JavaConverters._

object AuditLogJavaDao {

  val queries = DBSchema.queries.auditLogQueries

  def executeAll(db: Stream[JDBCIO, WriteOp]): Unit
  = RunWithDB.execute(Kleisli.liftF(db.flush.compile.drain))

  def removeEntriesForInstitution(institution: Institution): Unit =
    executeAll(queries.deleteForInst(institution))

  def removeEntriesBeforeDate(date: Date): Unit =
    executeAll(queries.deleteBefore(date.toInstant))

  def countForInstitution(institution: Institution): Long = RunWithDB.execute {
    Kleisli.liftF(queries.countForInst(institution).compile.last.map(_.getOrElse(0)))
  }

  def log(userId: String, sessionId: String, category: String,
          `type`: String, d1: String, d2: String, d3: String,
          d4: String, institution: Institution): Unit =
    logWithRequest(userId, sessionId, category, `type`, d1, d2, d3, d4, institution, null)

  def logWithRequest(userId: String, sessionId: String, category: String,
          `type`: String, d1: String, d2: String, d3: String,
          d4: String, institution: Institution, request: HttpServletRequest): Unit = {
    RunWithDB.execute( Kleisli {
      _ => queries.insertNew(id => AuditLogEntry(id, d1, d2, d3, Option(d4),
        category, `type`, sessionId, AuditLogMeta(referrer(request)), Instant.now(),
        UserId(userId), institution)).compile.drain
    })
  }

  def referrer(req: HttpServletRequest): Option[String] =
    Option(req).flatMap(r => Option(r.getHeader("Referer")))

  def logHttp(category: String,
          `type`: String, d1: String, d2: String, d3: String,
          d4: String, request: HttpServletRequest): Unit = {
    RunWithDB.execute( Kleisli {
      uc => queries.insertNew(id => AuditLogEntry(id, d1, d2, d3, Option(d4),
        category, `type`, uc.user.getSessionID, AuditLogMeta(referrer(request)), Instant.now(),
        UserId(uc.user.getUserBean.getUniqueID), uc.inst)).compile.drain
    })
  }

  def writeExport(folder: SubTemporaryFile, perFile: Int, inst: Institution,
                  progress: DefaultMessageCallback, xmlHelper: XmlHelper, xstream: XStream): Unit = RunWithDB.execute {
    Kleisli.liftF {
      queries.listForInst(inst).segmentN(perFile).zipWithIndex.map {
        a =>
          val xmlList = new util.ArrayList(a._1.force.toVector.map {
          ale =>
            val xml = new AuditLogEntryXml()
            xml.timestamp = new Date(ale.timestamp.toEpochMilli)
            xml.eventCategory = ale.event_category.value
            xml.eventType = ale.event_type.value
            xml.data1 = ale.data1.map(_.value).orNull
            xml.data2 = ale.data2.map(_.value).orNull
            xml.data3 = ale.data3.map(_.value).orNull
            xml.data4 = ale.data4.orNull
            xml.userId = ale.user_id.id
            xml.sessionId = ale.session_id.value
            xml
        }.asJava)

          xmlHelper.writeXmlFile(folder, s"${a._2}.xml", xmlList, xstream)
          progress.incrementCurrent()
      }.compile.drain
    }
  }

  def insertFromXml(inst: Institution, entry: AuditLogEntryXml): Unit = RunWithDB.execute { Kleisli.liftF {
    queries.insertNew(id => AuditLogEntry(id, entry.data1, entry.data2, entry.data3, Option(entry.data4), entry.eventCategory, entry.eventType,
      entry.sessionId, AuditLogMeta(), entry.timestamp.toInstant, UserId(entry.userId), inst)).compile.drain
    }
  }
}
