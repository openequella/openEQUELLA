package com.tle.core.item.standard

import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation
import com.tle.core.notification.beans.Notification
import com.tle.core.services.user.UserPreferenceService

import scala.collection.JavaConverters._

object NotifyMyLive {

  def notifyOwners(op: AbstractStandardWorkflowOperation, ups: UserPreferenceService): Unit = {
    val item = op.getItem
    val users = ups.getPreferenceForUsers(UserPreferenceService.NOTIFY_MYLIVE,
      (item.getCollaborators.asScala.toBuffer :+ item.getOwner).asJavaCollection).asScala.collect {
      case (u, "true") => u
    }
    op.addNotifications(item.getItemId, users.asJavaCollection, Notification.REASON_MYLIVE, false)
  }

}
