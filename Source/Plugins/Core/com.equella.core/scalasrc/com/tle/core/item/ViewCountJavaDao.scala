package com.tle.core.item

import java.time.Instant

import cats.data.Kleisli
import com.tle.beans.item.ItemId
import com.tle.core.db.tables.{AttachmentViewCount, ItemViewCount}
import com.tle.core.db.{DBSchema, RunWithDB, UserContext}
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.syntax._

object ViewCountJavaDao {

  val queries = DBSchema.queries.viewCountQueries

  def incrementSummaryViews(itemKey: ItemId): Unit = RunWithDB.execute {
    Kleisli { uc =>
      queries.itemCount((uc.inst, itemKey.getUuid, itemKey.getVersion)).last.flatMap {
        case Some(c) => queries.writeItemCounts.update(c, c.copy(count = c.count + 1, last_viewed = Instant.now()))
        case _ =>
          val newCount = ItemViewCount(uc.inst, itemKey.getUuid, itemKey.getVersion, 1, Instant.now())
          queries.writeItemCounts.insert(newCount)
      }.flush.compile.drain
    }
  }

  def incrementAttachmentViews(itemKey: ItemId, attachment: String): Unit = RunWithDB.execute {
    Kleisli { uc =>
      queries.attachmentCount((uc.inst, itemKey.getUuid, itemKey.getVersion, attachment)).last.flatMap {
        case Some(c) => queries.writeAttachmentCounts.update(c, c.copy(count = c.count + 1, last_viewed = Instant.now()))
        case _ =>
          val newCount = AttachmentViewCount(uc.inst, itemKey.getUuid, itemKey.getVersion, attachment, 1, Instant.now())
          queries.writeAttachmentCounts.insert(newCount)
      }.flush.compile.drain
    }
  }

  def getSummaryViewCount(itemKey: ItemId): java.lang.Integer = RunWithDB.execute {
    Kleisli { uc : UserContext =>
      queries.itemCount((uc.inst, itemKey.getUuid, itemKey.getVersion)).map(_.count).compile.last
    }.map(_.getOrElse(0))
  }

  def getAttachmentViewCount(itemKey: ItemId, attachment: String): java.lang.Integer = RunWithDB.execute {
    Kleisli { uc : UserContext =>
      queries.attachmentCount((uc.inst, itemKey.getUuid, itemKey.getVersion, attachment)).map(_.count).compile.last
    }.map(_.getOrElse(0))
  }
}
