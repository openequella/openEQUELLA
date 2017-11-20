/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.notification.service

import java.util.concurrent.Callable
import java.util.{Collections, Date, UUID}

import com.tle.common.institution.CurrentInstitution
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.email.{EmailResult, EmailService}
import com.tle.core.notification.beans.Notification
import com.tle.core.notification.dao.NotificationDao
import com.tle.core.notification.service.NotificationEmailer._
import com.tle.core.notification.{EmailKey, NotificationExtension}
import com.tle.core.plugins.{AbstractPluginService, PluginTracker}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object NotificationEmailer {
  val MAX_EMAIL_NOTIFICATIONS = 30

  lazy val tracker: PluginTracker[NotificationExtension] = new PluginTracker(AbstractPluginService.get(), "com.tle.core.notification",
    "notificationExtension", "type").setBeanKey("bean")

  def extensionForType(str: String): Option[NotificationExtension] =
    Option(tracker.getExtension(str)).map(tracker.getBeanByExtension)

  val LOGGER = LoggerFactory.getLogger(classOf[NotificationEmailer])
}

class NotificationEmailer(batched: Boolean,
                          processDate: Date, attemptId: String, dao: NotificationDao, emailService: EmailService)
  extends Callable[java.lang.Iterable[Callable[EmailResult[EmailKey]]]] {


  override def call: java.lang.Iterable[Callable[EmailResult[EmailKey]]] = {
    val userBean = CurrentUser.getDetails
    val inst = CurrentInstitution.get()
    val user = userBean.getUniqueID
    dao.updateLastAttempt(user, batched, processDate, attemptId)
    val reasonMap = dao.getReasonCounts(user, attemptId).asScala.mapValues(_.intValue()).toMap
    val canSend = emailService.hasMailSettings && Option(userBean.getEmailAddress).exists(_.nonEmpty)
    val ext2Reasons = reasonMap.keys.groupBy(extensionForType)
    val emailCallers = ext2Reasons.flatMap { case (extO, reasons) =>
      (extO, canSend) match {
        case (Some(ext), true) =>
          val newestNotes = dao.getNewestNotificationsForUser(MAX_EMAIL_NOTIFICATIONS, user, reasons.asJavaCollection, attemptId).asScala
          ext.emails(userBean, newestNotes, reasonMap).map { ne =>
            val id = UUID.randomUUID()
            def successCB() : Unit = {
              val (p,d) = ne.pertainsTo.partition(n => ext.isIndexed(n.getReason))
              def ids(n: Iterable[Notification]) = n.map(_.getId.asInstanceOf[java.lang.Long]).asJavaCollection
              dao.deleteUnindexedById(user, ids(d), attemptId)
              dao.markProcessedById(user, ids(p), attemptId)
              if (LOGGER.isDebugEnabled)
              {
                LOGGER.debug(s"Successful sent email $id")
              }
            }
            if (LOGGER.isDebugEnabled) {
              LOGGER.debug(
                s"""EMAIL-ID: $id
                   |USER: ${userBean.getUsername}
                   |SUBJECT: ${ne.subject}
                   |CONTENT:
                   |${ne.text}
                 """.stripMargin)
            }
            val emailKey = EmailKey(id, userBean, inst, successCB)
            emailService.createEmailer(ne.subject, Collections.singletonList(userBean.getEmailAddress), ne.text, emailKey)
          }
        case (Some(ext), false) =>
          val (p, d) = reasons.partition(ext.isIndexed)
          dao.markProcessed(user, p.asJavaCollection, attemptId)
          dao.deleteUnindexed(user, d.asJavaCollection, attemptId)
          Seq.empty
        case (None, _) =>
          dao.deleteUnindexed(user, reasons.asJavaCollection, attemptId)
          Seq.empty
      }
    }
    emailCallers.asJava
  }
}