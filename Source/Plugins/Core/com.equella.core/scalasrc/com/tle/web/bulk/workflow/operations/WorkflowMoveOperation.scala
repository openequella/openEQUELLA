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

package com.tle.web.bulk.workflow.operations

import java.util.Date
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
class WorkflowMoveOperation @AssistedInject()(@Assisted("msg") val msg: String, @Assisted("toStep") val toStep: String) extends TaskOperation {

  @Inject var aclService: TLEAclManager = _


  override def execute: Boolean = {
    if (!aclService.checkPrivilege("MANAGE_WORKFLOW", getWorkflow)) {
      throw new AccessDeniedException(CurrentLocale.get("com.tle.core.services.item.error.nopriv", "MANAGE_WORKFLOW", getItemId))
    }

    clearAllStatuses()

    val nodeSeq = getWorkflow.getNodes.asScala.toSeq

    def requiresSiblingCompletion(wn: WorkflowNode) = wn.getType != WorkflowNode.PARALLEL_TYPE

    val parentMap = nodeSeq.groupBy(n => Option(n.getParent))

    def newStatus(completed: Boolean)(n: WorkflowNode) = {
      val ns = n.getType match {
        case WorkflowNode.ITEM_TYPE => val wis = new WorkflowItemStatus(n, null)
          wis.setStarted(params.getDateNow)
          wis
        case _ => new WorkflowNodeStatus(n)
      }
      ns.setStatus(if (completed) WorkflowNodeStatus.COMPLETE else WorkflowNodeStatus.INCOMPLETE)
      ns
    }

    nodeSeq.find(_.getUuid == toStep).foreach { wn =>

      def completePreviousSiblings(child: WorkflowNode): Seq[WorkflowNodeStatus] = (for {
        parent <- Option(child.getParent).filter(requiresSiblingCompletion)
        children <- parentMap.get(Some(parent))
      } yield {
        children.filter(_.getChildIndex < child.getChildIndex).map(newStatus(true))
      }).getOrElse(Seq.empty)

      def addParentStatuses(child: WorkflowNode): Seq[WorkflowNodeStatus] = {
        (completePreviousSiblings(child) :+ newStatus(false)(child)) ++
          Option(child.getParent).toSeq.flatMap(addParentStatuses)
      }

      val newStatuses = addParentStatuses(wn)
      initStatusMap(newStatuses.asJava)
      newStatuses.foreach { wns =>
        if (wns.getStatus == WorkflowNodeStatus.INCOMPLETE && !wns.getNode.isLeafNode) {
          update(wns.getNode)
        }
      }
      enter(wn)
      val h = createHistory(HistoryEvent.Type.taskMove)
      setToStepFromTask(h, toStep)
      h.setComment(msg)
    }
    updateModeration()
    true
  }
}
