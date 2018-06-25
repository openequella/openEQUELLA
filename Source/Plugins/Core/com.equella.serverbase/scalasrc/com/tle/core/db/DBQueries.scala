package com.tle.core.db

import com.tle.core.db.tables.AuditLogEntry
import com.tle.core.db.types.UserId
import fs2.Stream
import io.doolse.simpledba.{WriteOp, WriteQueries}
import io.doolse.simpledba.jdbc.JDBCIO

case class AuditLogQueries(insertNew: (Long => AuditLogEntry) => Stream[JDBCIO, AuditLogEntry],
                           deleteForUser : ((UserId, Long)) => Stream[JDBCIO, WriteOp],
                          listForUser: ((UserId, Long)) => Stream[JDBCIO, AuditLogEntry])

trait DBQueries {

  val auditLogQueries : AuditLogQueries

}
