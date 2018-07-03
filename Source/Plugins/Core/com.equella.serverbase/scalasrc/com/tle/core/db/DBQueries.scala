package com.tle.core.db

import java.time.Instant

import com.tle.core.db.tables.{AttachmentViewCount, AuditLogEntry, ItemViewCount}
import com.tle.core.db.types.{DbUUID, InstId, UserId}
import fs2.Stream
import io.doolse.simpledba.{WriteOp, WriteQueries}
import io.doolse.simpledba.jdbc.{JDBCColumn, JDBCIO, JDBCSQLConfig}
import org.slf4j.LoggerFactory

case class AuditLogQueries(insertNew: (Long => AuditLogEntry) => Stream[JDBCIO, AuditLogEntry],
                           deleteForUser : ((UserId, InstId)) => Stream[JDBCIO, WriteOp],
                          listForUser: ((UserId, InstId)) => Stream[JDBCIO, AuditLogEntry],
                           deleteForInst: InstId => Stream[JDBCIO, WriteOp],
                           deleteBefore: Instant => Stream[JDBCIO, WriteOp],
                           countForInst: InstId => Stream[JDBCIO, Int],
                           listForInst: InstId => Stream[JDBCIO, AuditLogEntry])

case class ViewCountQueries(writeItemCounts: WriteQueries[JDBCIO, ItemViewCount],
                            writeAttachmentCounts: WriteQueries[JDBCIO, AttachmentViewCount],
                            itemCount: ((InstId, DbUUID, Int)) => Stream[JDBCIO, ItemViewCount],
                            attachmentCount: ((InstId, DbUUID, Int, DbUUID)) => Stream[JDBCIO, AttachmentViewCount],
                            countForCollectionId: Long => Stream[JDBCIO, Int],
                            attachmentCountForCollectionId: Long => Stream[JDBCIO, Int]
                           )

object DBQueries
{
  val logSQL = LoggerFactory.getLogger("org.hibernate.SQL")
}
trait DBQueries {

  type C[A] <: JDBCColumn

  def setupLogging(config: JDBCSQLConfig[C]): JDBCSQLConfig[C] =
    config.withPrepareLogger(sql => DBQueries.logSQL.debug(sql))

  val auditLogQueries : AuditLogQueries

  val viewCountQueries: ViewCountQueries

}
