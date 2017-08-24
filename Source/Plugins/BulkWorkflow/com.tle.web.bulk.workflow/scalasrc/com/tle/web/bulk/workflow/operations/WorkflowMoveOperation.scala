package com.tle.web.bulk.workflow.operations

import javax.inject.Inject

import com.google.inject.assistedinject.{Assisted, AssistedInject}
import com.tle.annotation.Nullable
import com.tle.beans.item.HistoryEvent
import com.tle.common.i18n.CurrentLocale
import com.tle.common.workflow.{WorkflowItemStatus, WorkflowNodeStatus}
import com.tle.common.workflow.node.WorkflowNode
import com.tle.core.item.NodeStatus
import com.tle.core.item.standard.ItemOperationFactory
import com.tle.core.item.standard.operations.workflow.TaskOperation
import com.tle.core.security.TLEAclManager
import com.tle.core.security.impl.SecureInModeration
import com.tle.exceptions.AccessDeniedException

import scala.collection.JavaConverters._

@SecureInModeration
class WorkflowMoveOperation @AssistedInject()(@Assisted("msg") val msg: String, @Assisted("toStep") @Nullable val toStep: String) extends TaskOperation {

  @Inject var aclService: TLEAclManager = _


  override def execute: Boolean = {
    if (!aclService.checkPrivilege("MANAGE_WORKFLOW", getWorkflow)) {
      throw new AccessDeniedException(CurrentLocale.get("com.tle.core.services.item.error.nopriv", "MANAGE_WORKFLOW", getItemId))
    }

    clearAllStatuses()

    val nodeSeq = getWorkflow.getNodes.asScala.toSeq
    val parentMap = nodeSeq.groupBy(n => Option(n.getParent))
    nodeSeq.find(_.getUuid == toStep).foreach { wn =>
      def addParentStatuses(child: WorkflowNode): List[WorkflowNodeStatus] = {
        val pareOp = Option(child.getParent)
        val prvIndex = child.getChildIndex - 1
        val otherStatuses = if (prvIndex < 0) pareOp.toList.flatMap(addParentStatuses)
        else parentMap.get(pareOp).flatMap(_.find(_.getChildIndex == prvIndex)).toList.flatMap(addParentStatuses)
        val newStatus = child.getType match {
          case WorkflowNode.ITEM_TYPE => new WorkflowItemStatus(child, null)
          case _ => new WorkflowNodeStatus(child)
        }
        val complete = pareOp.exists(_.getType == WorkflowNode.SERIAL_TYPE) && child != wn
        newStatus.setStatus(if (complete) WorkflowNodeStatus.COMPLETE else WorkflowNodeStatus.INCOMPLETE)
        newStatus :: otherStatuses
      }
      val newStatuses = addParentStatuses(wn)
      initStatusMap(newStatuses.asJava)
      enter(wn)
      val h = createHistory(HistoryEvent.Type.taskMove)
      setToStepFromTask(h, toStep)
      h.setComment(msg)
    }
    updateModeration()
    true
  }
}
