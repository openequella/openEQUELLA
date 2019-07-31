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

package com.tle.common.workflow;

import com.tle.common.workflow.node.WorkflowNode;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

@Entity
@AccessType("field")
@DiscriminatorValue("task")
public class WorkflowItemStatus extends WorkflowNodeStatus {
  private static final long serialVersionUID = 1L;

  @Index(name = "datedue_idx")
  private Date dateDue;

  private Date started;
  private boolean overdue;

  @Column(length = 40)
  private String assignedTo;

  @ElementCollection
  @JoinTable(
      name = "WorkflowNodeStatusAccepted",
      joinColumns = @JoinColumn(name = "workflow_node_status_id"))
  @Column(name = "`user`", length = 255)
  private Set<String> acceptedUsers = new HashSet<String>();

  @ManyToOne
  @Index(name = "cause_idx")
  private WorkflowNodeStatus cause;

  public WorkflowItemStatus() {
    super();
  }

  public WorkflowItemStatus(WorkflowNode node, WorkflowNodeStatus cause) {
    super(node);
    this.cause = cause;
  }

  public Set<String> getAcceptedUsers() {
    return acceptedUsers;
  }

  public void addAccepted(String userId) {
    acceptedUsers.add(userId);
  }

  public String getAssignedTo() {
    return assignedTo;
  }

  public void setAssignedTo(String assignedTo) {
    this.assignedTo = assignedTo;
  }

  public Date getDateDue() {
    return dateDue;
  }

  public void setDateDue(Date dateDue) {
    this.dateDue = dateDue;
  }

  public WorkflowNodeStatus getCause() {
    return cause;
  }

  public void setCause(WorkflowNodeStatus cause) {
    this.cause = cause;
  }

  @Override
  public void archive() {
    super.archive();
    acceptedUsers.clear();
    dateDue = null;
  }

  public boolean isOverdue() {
    return overdue;
  }

  public void setOverdue(boolean overdue) {
    this.overdue = overdue;
  }

  public Date getStarted() {
    return started;
  }

  public void setStarted(Date started) {
    this.started = started;
  }
}
