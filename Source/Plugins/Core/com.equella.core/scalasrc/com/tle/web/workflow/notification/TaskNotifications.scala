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

package com.tle.web.workflow.notification

import javax.inject.{Inject, Singleton}

import com.dytech.devlib.PropBagEx
import com.dytech.edge.common.Constants
import com.tle.beans.item.Item
import com.tle.common.NameValue
import com.tle.common.usermanagement.user.valuebean.UserBean
import com.tle.common.workflow.node.WorkflowItem
import com.tle.common.workflow.node.WorkflowItem.AutoAction
import com.tle.common.workflow.{WorkflowMessage, WorkflowNodeStatus}
import com.tle.core.filesystem.WorkflowMessageFile
import com.tle.core.guice.Bind
import com.tle.core.institution.InstitutionService
import com.tle.core.notification.beans.Notification
import com.tle.core.services.FileSystemService
import com.tle.core.services.user.{LazyUserLookup, UserService}
import com.tle.core.workflow.service.WorkflowService
import com.tle.web.sections.render.{AppendedLabel, Label}
import com.tle.web.sections.result.util._
import com.tle.web.workflow.notification.NotificationLangStrings._
import com.tle.web.workflow.servlet.WorkflowMessageServlet

import scala.collection.JavaConverters._

object TaskNotifications {

  import NotificationLangStrings.{l, r}

  val LABEL_CAUSEREJECTED = l("email.rejecttask")
  val LABEL_CAUSEACCEPTED = l("email.accepttask")
  val KEY_MESSAGELABEL = r.key("email.msg.")
  val KEY_FILEMESSAGE = r.key("email.file.")

  val KEY_AUTOMSG = r.key("email.automsg")
  var KEY_AUTODAYS = r.key("email.autodays")
  val LABEL_AUTOREJECT = l("email.autoreject")
  val LABEL_AUTOACCEPT = l("email.autoaccept")
}

import com.tle.web.workflow.notification.TaskNotifications._

@Bind
@Singleton
class TaskNotifications extends FilterableNotification with NotificationLookup with TemplatedNotification {

  type N = TaskNoteModel
  @Inject
  var workflowService: WorkflowService = _
  @Inject
  var userService: UserService = _
  @Inject
  var institutionService: InstitutionService = _
  @Inject
  var fileSystemService: FileSystemService = _

  override def isIndexed(ntype: String) = ntype != Notification.REASON_MODERATE

  class Message(lazyLookup: LazyUserLookup, msg: WorkflowMessage) {
    def getMessage = msg.getMessage

    def getBy = new UserLabel(msg.getUser, lazyLookup)

    def getLabel = new KeyLabel(KEY_MESSAGELABEL + msg.getType)

    def getFileLabel = new KeyLabel(KEY_FILEMESSAGE + msg.getType)

    def date = msg.getDate

    val uuid = msg.getUuid
    val getFiles = fileSystemService.enumerate(new WorkflowMessageFile(uuid), Constants.BLANK, null).map {
      fe => new NameValue(fe.getName, WorkflowMessageServlet.messageUrl(uuid, fe.getName))
    }
    val isHasFiles = getFiles.nonEmpty


  }

  val msgTypes = Set(WorkflowMessage.TYPE_ACCEPT, WorkflowMessage.TYPE_REJECT, WorkflowMessage.TYPE_SUBMIT)

  case class TaskGroup(multiple: Boolean, rejected: Boolean, taskName: String) extends NotificationGroup
  {
    def groupName = s"task.${if (multiple) "m" else "s"}${if (rejected) "r" else "a"}"

    def subjectLabel: Label = new KeyLabel(KEYPFX_EMAIL_SUBJECT+groupName, taskName)

    def templateName: String = "notification-tasks.ftl"

    def headerHello(user: UserBean): Label = NotificationLangStrings.userHeaderLabel(user)

    def headerReason(total: Int): Label = new KeyLabel(NotificationLangStrings.pluralKey(NotificationLangStrings.KEY_HEADER+groupName, total), taskName)
  }

  case class TaskNoteModel(lul: LazyUserLookup)(val note: Notification, val item: Item) extends TaskNotification with OwnerLookup {
    val status = Option(workflowService.getIncompleteStatus(itemTaskId))
    val currentTask: Option[WorkflowItem] = workflowItem(taskId).collect {
      case wi: WorkflowItem => wi
    }
    val getAutoAction = currentTask.flatMap { workflowItem =>
      Option(workflowItem.getAutoAction).collect {
        case AutoAction.ACCEPT => LABEL_AUTOACCEPT
        case AutoAction.REJECT => LABEL_AUTOREJECT
      }.map { l => new KeyLabel(KEY_AUTOMSG, l, new PluralKeyLabel(KEY_AUTODAYS, workflowItem.getActionDays)) }
    }.orNull
    val causeStep = status.flatMap(wis => Option(wis.getCause))
    val causeAccepted = causeStep.filter(_.getNode.getParent != null).map(_.getStatus == WorkflowNodeStatus.COMPLETE)

    def getCauseTask = causeStep.map(wns => new BundleLabel(wns.getNode.getName, bundleCache)).orNull

    def getCauseLabel = causeAccepted.map(if (_) LABEL_CAUSEACCEPTED else LABEL_CAUSEREJECTED).orNull

    val getMessages = causeStep.map(_.getComments.asScala.collect {
      case c if msgTypes(c.getType) => new Message(lul, c)
    }.toSeq.sortBy(_.date)).getOrElse(Seq.empty).asJava

    def isHasCauseInfo = causeStep.isDefined && (causeAccepted.isDefined || !getMessages.isEmpty)

    def getDueDate = status.map(_.getDateDue).orNull

    def isSingleModerator : Boolean =
      currentTask.map(wi => workflowService.getAllModeratorUserIDs(new PropBagEx(item.getItemXml.getXml), wi))
        .exists(_.size() == 1)

    override def group = note.getReason match {
      case Notification.REASON_OVERDUE => StdNotificationGroup("notification-overdue.ftl", Notification.REASON_OVERDUE)
      case _ => TaskGroup(!isSingleModerator, causeAccepted.exists(!_), getTaskName.getText)
    }
  }

  def toFreemarkerModel(notes: Iterable[Notification]) = createDataIgnore(notes, TaskNoteModel(new LazyUserLookup(userService)))
}
