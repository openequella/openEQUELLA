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
    if (!shouldProcessWorkflow(item, workflow)) {
      return false;
    }

    boolean hasWorkflowChanged = processWorkflowTasks(workflow);
    if (hasWorkflowChanged) {
      updateModeration();
    }
    return hasWorkflowChanged;
  }

  private boolean shouldProcessWorkflow(Item item, Workflow workflow) {
    return workflow != null && item.isModerating();
  }

  private boolean processWorkflowTasks(Workflow workflow) {
    return processAllTasks(workflow.getRoot(), resolveOriginalItemXml());
  }

  /**
   * Resolves the item XML to use as the pre-edit metadata baseline for moderator comparison.
   *
   * <p>Direct edits using REST API overwrite the in-memory item XML before this operation runs, so
   * they pass the original XML through {@link ItemOperationParams#ATTRIBUTE_ORIGINAL_ITEM_XML}.
   * Standard workflow save paths can fall back to the entity XML because the check runs before the
   * new XML is written there.
   */
  private PropBagEx resolveOriginalItemXml() {
    return Optional.ofNullable(
            getParams().getAttributes().get(ItemOperationParams.ATTRIBUTE_ORIGINAL_ITEM_XML))
        .filter(StringUtils::isNotEmpty)
        .map(PropBagEx::new)
        .or(this::getItemXmlAsPropBag)
        .or(() -> Optional.ofNullable(getItemXml()))
        .orElseGet(PropBagEx::new);
  }

  /**
   * Refreshes the task assignment by comparing the original and current moderators to detect stale
   * metadata-derived assignments and repair them if needed.
   */
  private boolean refreshAssignment(
      TaskStatus taskStatus, WorkflowItem task, PropBagEx originalItemXml) {
    Set<String> originalModerators = resolveOriginalModerators(originalItemXml, task);
    return taskStatus.reassignIfStale(originalModerators);
  }

  private Set<String> resolveOriginalModerators(PropBagEx originalItemXml, WorkflowItem task) {
    return workflowService.getAllModeratorUserIDs(originalItemXml, task);
  }

  /**
   * Walks the workflow tree and updates incomplete nodes.
   *
   * <p>For incomplete item-task nodes, refreshes the persisted assignment before running the normal
   * task update. Script nodes keep the existing update behavior. The same pre-edit XML is passed
   * through the traversal so each task can resolve its original moderator set consistently.
   */
  private boolean processAllTasks(WorkflowNode node, PropBagEx originalItemXml) {
    if (!shouldProcessNode(node)) {
      return false;
    }

    boolean updated = processCurrentNode(node, originalItemXml);
    updated |= processChildNodes(node, originalItemXml);
    return updated;
  }

  private boolean shouldProcessNode(WorkflowNode node) {
    NodeStatus nodeStatus = getNodeStatus(node.getUuid());
    return nodeStatus != null && nodeStatus.getStatus() == WorkflowNodeStatus.INCOMPLETE;
  }

  private boolean processCurrentNode(WorkflowNode node, PropBagEx originalItemXml) {
    NodeStatus nodeStatus = getNodeStatus(node.getUuid());
    char type = getNodeType(nodeStatus);
    if (type == WorkflowNode.ITEM_TYPE) {
      return processItemTask(node, nodeStatus, originalItemXml);
    } else if (type == WorkflowNode.SCRIPT_TYPE) {
      return processScriptTask(node);
    }
    return false;
  }

  private boolean processItemTask(
      WorkflowNode node, NodeStatus nodeStatus, PropBagEx originalItemXml) {
    WorkflowItem task = (WorkflowItem) nodeStatus.getBean().getNode();
    boolean updated = refreshAssignment((TaskStatus) nodeStatus, task, originalItemXml);
    updated |= update(node);
    return updated;
  }

  private boolean processScriptTask(WorkflowNode node) {
    return update(node);
  }

  private boolean processChildNodes(WorkflowNode node, PropBagEx originalItemXml) {
    boolean updated = false;
    if (!node.isLeafNode()) {
      NodeStatus nodeStatus = getNodeStatus(node.getUuid());
      char type = getNodeType(nodeStatus);
      NodeStatus[] childStatuses = getChildStatuses(node);
      WorkflowTreeNode treenode = (WorkflowTreeNode) node;
      int num = treenode.numberOfChildren();
      for (int i = 0; i < num; i++) {
        WorkflowNode child = treenode.getChild(i);
        if (type == WorkflowNode.PARALLEL_TYPE && childStatuses[i] == null) {
          updated |= update(node);
        }
        updated |= processAllTasks(child, originalItemXml);
      }
    }
    return updated;
  }

  private char getNodeType(NodeStatus nodeStatus) {
    return nodeStatus.getBean().getNode().getType();
  }
}
