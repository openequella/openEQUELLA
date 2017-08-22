package com.tle.web.workflow.notification

import com.tle.web.sections.result.util.KeyLabel
import FilterableNotification._
import com.tle.web.notification.WebNotificationExtension

object FilterableNotification
{
  import NotificationLangStrings.{r,l}

  val KEY_REASON_FILTER = r.key("notereason.")
  val KEY_REASON_LIST = r.key("notificationlist.reasons.")
}

trait FilterableNotification extends WebNotificationExtension {
  def getReasonLabel(str: String) = new KeyLabel(KEY_REASON_LIST + str)
  def getReasonFilterLabel(str: String) = new KeyLabel(KEY_REASON_FILTER + str)
  def isIndexed(ntype: String) = true
}
