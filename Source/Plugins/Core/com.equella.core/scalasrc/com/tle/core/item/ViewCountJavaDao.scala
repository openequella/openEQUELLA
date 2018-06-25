package com.tle.core.item

import com.tle.beans.item.ItemId

object ViewCountJavaDao {

  def incrementSummaryViews(itemKey: ItemId): Unit = {
    ???
  }

  def incrementAttachmentViews(itemKey: ItemId, attachment: String): Unit = {
    ???
  }

  def getSummaryViewCount(itemKey: ItemId): java.lang.Integer = {
    ???
  }

  def getAttachmentViewCount(itemKey: ItemId, attachment: String): java.lang.Integer = {
    ???
  }
}
