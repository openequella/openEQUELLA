package com.tle.web.workflow.notification

import java.io.StringWriter
import javax.inject.{Inject, Singleton}

import com.tle.beans.item.Item
import com.tle.common.usermanagement.user.valuebean.UserBean
import com.tle.common.workflow.node.WorkflowItem
import com.tle.common.workflow.node.WorkflowItem.AutoAction
import com.tle.common.workflow.{WorkflowMessage, WorkflowNodeStatus}
import com.tle.core.guice.Bind
import com.tle.core.notification.NotificationEmail
import com.tle.core.notification.beans.Notification
import com.tle.core.notification.standard.service.NotificationPreferencesService
import com.tle.core.services.user.{LazyUserLookup, UserService}
import com.tle.core.workflow.service.WorkflowService
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory
import com.tle.web.sections.render.{AppendedLabel, Label}
import com.tle.web.sections.result.util._
import com.tle.web.workflow.notification.NotificationLangStrings._

import scala.collection.JavaConverters._

object TaskNotifications {

  import NotificationLangStrings.{l, r}

  val LABEL_CAUSEREJECTED = l("email.rejecttask")
  val LABEL_CAUSEACCEPTED = l("email.accepttask")
  val KEY_MESSAGELABEL = r.key("email.msg.")

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

  override def isIndexed(ntype: String) = ntype != Notification.REASON_MODERATE

  class Message(lazyLookup: LazyUserLookup, msg: WorkflowMessage) {
    def getMessage = msg.getMessage

    def getBy = new UserLabel(msg.getUser(), lazyLookup)

    def getLabel = new KeyLabel(KEY_MESSAGELABEL + msg.getType())

    def date = msg.getDate
  }

  val msgTypes = Set(WorkflowMessage.TYPE_ACCEPT, WorkflowMessage.TYPE_REJECT, WorkflowMessage.TYPE_SUBMIT)

  case class TaskNoteModel(lul: LazyUserLookup)(val note: Notification, val item: Item) extends TaskNotification {
    val status = Option(workflowService.getIncompleteStatus(itemTaskId))
    val currentTask: Option[WorkflowItem] = workflowItem(taskId)
    val autoAction: Option[Label] = currentTask.flatMap { workflowItem =>
      Option(workflowItem.getAutoAction).collect {
        case AutoAction.ACCEPT => LABEL_AUTOACCEPT
        case AutoAction.REJECT => LABEL_AUTOREJECT
      }.map { l => new KeyLabel(KEY_AUTOMSG, l, new PluralKeyLabel(KEY_AUTODAYS, workflowItem.getActionDays)) }
    }
    val causeStep = status.flatMap(wis => Option(wis.getCause)).filter(_.getNode.getParent != null)
    val causeAccepted = causeStep.map(_.getStatus == WorkflowNodeStatus.COMPLETE)

    def getCauseTask = causeStep.map(wns => new BundleLabel(wns.getNode.getName, bundleCache)).orNull

    def getCauseLabel = causeAccepted.map(if (_) LABEL_CAUSEACCEPTED else LABEL_CAUSEREJECTED).orNull

    def getMessages = causeStep.map(_.getComments.asScala.collect {
      case c if msgTypes(c.getType) => new Message(lul, c)
    }.toSeq.sortBy(_.date)).getOrElse(Seq.empty).asJava

    def getDueDate = status.map(_.getDateDue).orNull

    override def group = note.getReason match {
      case Notification.REASON_MODERATE => causeAccepted.map(if (_) "taskaccepted" else "taskrejected").getOrElse("taskother")
      case o => o
    }
  }
  override def templateName: String = "notification-tasks.ftl"

  def toFreemarkerModel(notes: Iterable[Notification]) = createDataIgnore(notes, TaskNoteModel(new LazyUserLookup(userService)))
}
