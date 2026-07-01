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

package com.tle.core.item.standard.workflow.nodes;

import com.google.common.collect.Sets;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.util.Dates;
import com.tle.common.util.LocalDate;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.ScriptNode;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowItem.MoveLive;
import com.tle.common.workflow.node.WorkflowItem.Priority;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.common.workflow.node.WorkflowTreeNode;
import com.tle.core.item.NodeStatus;
import com.tle.core.item.operations.ItemOperationParams;
import com.tle.core.item.standard.operations.workflow.TaskOperation;
import com.tle.core.notification.beans.Notification;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TaskStatus extends AbstractNodeStatus {
  private final WorkflowItemStatus taskbean;

  public TaskStatus(WorkflowNodeStatus bean, TaskOperation op) {
    super(bean, op);
    if (!(bean instanceof WorkflowItemStatus)) {
      throw new ClassCastException(
          "bean required to be WorkflowItemStatus, but is " //$NON-NLS-1$
              + bean.getClass().getSimpleName());
    }
    this.taskbean = (WorkflowItemStatus) bean;
  }

  public boolean canCurrentUserModerate(TaskOperation op) {
    return op.canCurrentUserModerate((WorkflowItem) node, taskbean);
  }

  public Set<String> getUsersToModerate(TaskOperation op) {
    return op.getUsersToModerate((WorkflowItem) node);
  }

  @Override
  public boolean update() {
    WorkflowItem item = (WorkflowItem) node;
    Set<String> acceptedUsers = taskbean.getAcceptedUsers();
    Set<String> usersToModerate = op.getUsersToModerate(item);
    boolean unan = item.isUnanimousacceptance();
    if (unan) {
      if (acceptedUsers.containsAll(usersToModerate)) {
        return finished();
      }
    } else if (!acceptedUsers.isEmpty()
        || (usersToModerate.isEmpty() && Check.isEmpty(item.getRoles()))) {
      return finished();
    }

    return false;
  }

  @Override
  public boolean finished() {
    clear();
    final WorkflowItem task = (WorkflowItem) node;
    if (task.getMovelive() == MoveLive.ACCEPTED) {
      op.makeLive(false);
    }
    return super.finished();
  }

  @Override
  public void clear() {
    op.removeNotificationsForKey(
        getTaskKey(), Notification.REASON_MODERATE, Notification.REASON_OVERDUE);
    if (taskbean.getStatus() == WorkflowNodeStatus.INCOMPLETE) {
      op.exitTask((WorkflowItem) node);
    }
  }

  public void addAccepted(String userId) {
    op.removeNotificationForUserAndKey(
        getTaskKey(), userId, Notification.REASON_MODERATE, Notification.REASON_OVERDUE);
    taskbean.addAccepted(userId);
  }

  public void setAssignedTo(String userId) {
    taskbean.setAssignedTo(userId);
  }

  public String getAssignedTo() {
    return taskbean.getAssignedTo();
  }

  /**
   * Repairs metadata-derived task assignments after item metadata changes.
   *
   * <p>Only reassigns when the eligible moderator set has changed and the current assignee appears
   * stale. Assignees still in the current moderator set are preserved, as are likely manual
   * assignments made by users who could moderate outside the metadata-derived moderator set.
   *
   * <p>One known case is the system/super user, such as {@code TLE_ADMINISTRATOR}. {@code
   * WorkflowServiceImpl.canCurrentUserModerate} allows system users to moderate regardless of the
   * task moderator list, so their self-assignment can update persisted {@code assignedTo} without
   * changing item XML.
   *
   * @param originalModerators moderators resolved from item XML before the metadata edit
   * @return {@code true} if the persisted {@code assignedTo} value changed
   */
  public boolean reassignIfStale(Set<String> originalModerators) {
    WorkflowItem task = (WorkflowItem) node;
    Set<String> currentModerators = op.getUsersToModerate(task);

    String originalAssignee = getAssignedTo();

    if (shouldKeepAssignee(originalAssignee, originalModerators, currentModerators)) {
      return false;
    }

    setAssignedTo(null);
    processAutoAssign(task, currentModerators);
    return !Objects.equals(originalAssignee, getAssignedTo());
  }

  /**
   * Determines whether the current assignee should be kept based on the original and current
   * moderator sets. Preserves assignees that are still current moderators or appear to be
   * manual/admin assignments.
   */
  private boolean shouldKeepAssignee(
      String assignee, Set<String> originalModerators, Set<String> currentModerators) {
    boolean areModeratorSetsUnchanged = Objects.equals(originalModerators, currentModerators);
    if (areModeratorSetsUnchanged) {
      return true;
    }

    boolean isAssigneeCurrentModerator = isAssignedToCurrentModerators(assignee, currentModerators);
    boolean isAssigneeManualOverride = isManualAssignment(assignee, originalModerators);
    return isAssigneeCurrentModerator || isAssigneeManualOverride;
  }

  private boolean isAssignedToCurrentModerators(String assignee, Set<String> currentModerators) {
    return !Check.isEmpty(assignee) && currentModerators.contains(assignee);
  }

  /**
   * Detects manual task assignment overrides that should survive metadata refresh.
   *
   * <p>Metadata-driven assignments come from item XML and workflow config, so the assignee is
   * normally in {@code originalModerators}. Manual overrides do not change item XML; they only
   * update persisted {@code assignedTo}.
   *
   * <p>Typical case: a user with {@code MANAGE_WORKFLOW} (often a system administrator) opens a
   * task they are not eligible to moderate from metadata and clicks <em>assign to me</em>. That
   * sets {@code assignedTo} to themselves even though they were never in the metadata moderator
   * pool. When metadata later changes, that assignment must be preserved.
   */
  private boolean isManualAssignment(String assignee, Set<String> originalModerators) {
    return !Check.isEmpty(assignee)
        && originalModerators != null
        && !originalModerators.contains(assignee);
  }

  @Override
  public void enter() {
    bean.setStatus(WorkflowNodeStatus.INCOMPLETE);
    taskbean.setStarted(op.getParams().getDateNow());
    final WorkflowItem task = (WorkflowItem) node;
    Set<String> usersToModerate = op.getUsersToModerate(task);
    op.addModerationNotifications(
        getTaskKey(),
        usersToModerate,
        Notification.REASON_MODERATE,
        task.getPriority() <= Priority.LOW.intValue());

    op.enterTask(task);

    processAutoAssign(task, usersToModerate);
    if (task.getMovelive() == MoveLive.ARRIVAL) {
      op.makeLive(false);
    }
    if (task.isEscalate()) {
      setupDueDate(task);
    }
    update();
  }

  private void processAutoAssign(WorkflowItem task, Set<String> usersToModerate) {
    final Item aitem = op.getItem();
    Set<String> autoAssignsByStep = task.getAutoAssigns();
    final String autoAssignByXPath = task.getAutoAssignNode();
    if (Check.isEmpty(autoAssignsByStep)) {
      autoAssignsByStep = findAllPreviousSteps();
    }

    // EQ-2388 shizzle
    if (usersToModerate.size() == 1) {
      setAssignedTo(usersToModerate.iterator().next());
    }

    boolean nonefound = true;
    for (String stepUuid : autoAssignsByStep) {
      NodeStatus nodeStatus = op.getNodeStatus(stepUuid);
      if (nodeStatus != null) {
        String assignedTo = ((TaskStatus) nodeStatus).getAssignedTo();
        if (!Check.isEmpty(assignedTo) && usersToModerate.contains(assignedTo)) {
          setAssignedTo(assignedTo);
          nonefound = false;
          break;
        }
      }
    }
    if (nonefound
        && autoAssignsByStep.contains("") // $NON-NLS-1$
        && usersToModerate.contains(aitem.getOwner())) {
      setAssignedTo(aitem.getOwner());
    } else if (!Check.isEmpty(autoAssignByXPath)) {
      setAssignedTo(op.getItemXml().getNode(autoAssignByXPath));
    }
  }

  private void setupDueDate(WorkflowItem task) {
    ItemOperationParams params = op.getParams();
    Date dateDue = null;
    if (!Check.isEmpty(task.getDueDatePath())) {
      String dateStr = op.getItemXml().getNode(task.getDueDatePath());
      if (!Check.isEmpty(dateStr)) {
        try {
          dateDue = new LocalDate(dateStr, Dates.ISO_DATE_ONLY, CurrentTimeZone.get()).toDate();
        } catch (ParseException e) {
          // ignore bad dates
        }
      }
    }
    if (dateDue == null) {
      dateDue =
          new Date(
              params.getDateNow().getTime() + TimeUnit.DAYS.toMillis(task.getEscalationdays()));
    }
    taskbean.setDateDue(dateDue);
  }

  private Set<String> findAllPreviousSteps() {
    Set<String> previousSteps = Sets.newHashSet();
    WorkflowNode currnode = node;
    WorkflowNode parent = node.getParent();
    while (parent != null) {
      if (parent.canHaveSiblingRejectPoints()) {
        int i = parent.indexOfChild(currnode) - 1;
        while (i >= 0) {
          WorkflowNode child = parent.getChild(i);
          if (child.getType() == WorkflowNode.ITEM_TYPE) {
            previousSteps.add(child.getUuid());
          }
          i--;
        }
      }

      currnode = parent;
      parent = parent.getParent();
    }
    return previousSteps;
  }

  private ItemTaskId getTaskKey() {
    return new ItemTaskId(op.getItem().getItemId(), node.getUuid());
  }

  public WorkflowNode getRejectNode(String taskid) {
    WorkflowNode currnode = node;
    WorkflowNode parent = node.getParent();
    while (parent != null) {
      if (parent.getUuid().equals(taskid)) {
        return parent.isRejectPoint() ? parent : null;
      }

      if (parent.canHaveSiblingRejectPoints()) {
        int i = parent.indexOfChild(currnode) - 1;
        while (i >= 0) {
          WorkflowNode child = parent.getChild(i);
          if (child.getUuid().equals(taskid) && isNodeRejectPoint(child)) {
            return child;
          }
          i--;
        }
      }

      currnode = parent;
      parent = parent.getParent();
    }
    return null;
  }

  public WorkflowNode getClosestRejectNode() {
    WorkflowNode currnode = node;
    WorkflowNode parent = node.getParent();
    while (parent != null) {
      if (parent.isRejectPoint() && !(parent instanceof ScriptNode)) {
        return parent;
      }

      if (parent.canHaveSiblingRejectPoints()) {
        int i = parent.indexOfChild(currnode) - 1;
        while (i >= 0) {
          WorkflowNode child = parent.getChild(i);
          if (isNodeRejectPoint(child) && !(child instanceof ScriptNode)) {
            return child;
          }
          i--;
        }
      }
      currnode = parent;
      parent = parent.getParent();
    }
    return null;
  }

  private boolean isNodeRejectPoint(WorkflowNode node) {
    if (node instanceof WorkflowItem) {
      return ((WorkflowItem) node).isRejectPoint();
    } else if (node instanceof WorkflowTreeNode) {
      return ((WorkflowTreeNode) node).isRejectPoint();
    }
    return false;
  }

  public Collection<String> getUsersLeft(TaskOperation op) {
    WorkflowItem item = (WorkflowItem) node;
    Set<String> acceptedUsers = taskbean.getAcceptedUsers();
    Set<String> usersToModerate = op.getUsersToModerate(item);
    if (usersToModerate == null) {
      return Collections.emptyList();
    }
    usersToModerate.removeAll(acceptedUsers);
    return usersToModerate;
  }
}
