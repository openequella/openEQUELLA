/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.item.standard.operations.workflow;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.Item;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.common.workflow.node.WorkflowTreeNode;
import com.tle.core.guice.Bind;
import com.tle.core.item.NodeStatus;
import com.tle.core.item.operations.ItemOperationParams;
import com.tle.core.item.standard.workflow.nodes.TaskStatus;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

@Bind
public class CheckStepOperation extends TaskOperation {

  protected CheckStepOperation() {
    // hide it
  }

  @Override
  public boolean execute() {
    Item item = getItem();
    // This is here for if the item is being purged
    if (item == null) {
      return false;
    }
    Workflow workflow = getWorkflow();
    if (workflow != null
        && item.isModerating()
        && checkAllTasks(workflow.getRoot(), resolvePreviousItemXml())) {
      updateModeration();
      return true;
    }
    return false;
  }

  /**
   * Resolves the item XML to use as the pre-edit metadata baseline for moderator comparison.
   *
   * <p>Direct edits using REST API overwrite the in-memory item XML before this operation runs, so
   * they pass the original XML through {@link ItemOperationParams#ATTRIBUTE_PREVIOUS_ITEM_XML}.
   * Standard workflow save paths can fall back to the entity XML because the check runs before the
   * new XML is written there.
   */
  private PropBagEx resolvePreviousItemXml() {
    return Optional.ofNullable(
            getParams().getAttributes().get(ItemOperationParams.ATTRIBUTE_PREVIOUS_ITEM_XML))
        .filter(StringUtils::isNotEmpty)
        .map(PropBagEx::new)
        .or(() -> Optional.ofNullable(getItemXmlAsPropBag()))
        .or(() -> Optional.ofNullable(getItemXml()))
        .orElseGet(PropBagEx::new);
  }

  /**
   * Refreshes the task assignment by comparing previous and current moderators to detect stale
   * metadata-derived assignments and repair them if needed.
   */
  private boolean refreshAssignment(
      TaskStatus taskStatus, WorkflowItem task, PropBagEx previousItemXml) {
    Set<String> previousModerators = resolvePreviousModerators(previousItemXml, task);
    return taskStatus.reassignIfStale(previousModerators);
  }

  private Set<String> resolvePreviousModerators(PropBagEx previousItemXml, WorkflowItem task) {
    return workflowService.getAllModeratorUserIDs(previousItemXml, task);
  }

  /**
   * Walks the workflow tree and updates incomplete nodes.
   *
   * <p>For incomplete item-task nodes, refreshes the persisted assignment before running the normal
   * task update. Script nodes keep the existing update behavior. The same pre-edit XML is passed
   * through the traversal so each task can resolve its previous moderator set consistently.
   */
  private boolean checkAllTasks(WorkflowNode node, PropBagEx previousItemXml) {
    if (!shouldProcessNode(node)) {
      return false;
    }

    boolean updated = processCurrentNode(node, previousItemXml);
    updated |= processChildNodes(node, previousItemXml);
    return updated;
  }

  private boolean shouldProcessNode(WorkflowNode node) {
    NodeStatus nodeStatus = getNodeStatus(node.getUuid());
    return nodeStatus != null && nodeStatus.getStatus() == WorkflowNodeStatus.INCOMPLETE;
  }

  private boolean processCurrentNode(WorkflowNode node, PropBagEx previousItemXml) {
    NodeStatus nodeStatus = getNodeStatus(node.getUuid());
    char type = nodeStatus.getBean().getNode().getType();
    if (type == WorkflowNode.ITEM_TYPE) {
      return processItemTask(node, nodeStatus, previousItemXml);
    } else if (type == WorkflowNode.SCRIPT_TYPE) {
      return processScriptTask(node);
    }
    return false;
  }

  private boolean processItemTask(
      WorkflowNode node, NodeStatus nodeStatus, PropBagEx previousItemXml) {
    WorkflowItem task = (WorkflowItem) nodeStatus.getBean().getNode();
    boolean updated = refreshAssignment((TaskStatus) nodeStatus, task, previousItemXml);
    updated |= update(node);
    return updated;
  }

  private boolean processScriptTask(WorkflowNode node) {
    return update(node);
  }

  private boolean processChildNodes(WorkflowNode node, PropBagEx previousItemXml) {
    boolean updated = false;
    if (!node.isLeafNode()) {
      NodeStatus nodeStatus = getNodeStatus(node.getUuid());
      char type = nodeStatus.getBean().getNode().getType();
      NodeStatus[] childStatuses = getChildStatuses(node);
      WorkflowTreeNode treenode = (WorkflowTreeNode) node;
      int num = treenode.numberOfChildren();
      for (int i = 0; i < num; i++) {
        WorkflowNode child = treenode.getChild(i);
        if (type == WorkflowNode.PARALLEL_TYPE && childStatuses[i] == null) {
          updated |= update(node);
        }
        updated |= checkAllTasks(child, previousItemXml);
      }
    }
    return updated;
  }
}
