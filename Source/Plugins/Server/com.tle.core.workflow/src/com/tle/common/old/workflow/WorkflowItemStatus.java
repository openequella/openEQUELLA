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

package com.tle.common.old.workflow;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;

import com.tle.common.old.workflow.node.WorkflowNode;

@Entity
@AccessType("field")
@DiscriminatorValue("task")
public class WorkflowItemStatus extends WorkflowNodeStatus
{
	private static final long serialVersionUID = 1L;

	private Date dateDue;
	@Column(length = 40)
	private String assignedTo;
	@Type(type = "xstream_immutable")
	private Set<String> acceptedUsers = new HashSet<String>();

	public WorkflowItemStatus()
	{
		super();
	}

	public WorkflowItemStatus(WorkflowNode node)
	{
		super(node);
	}

	public Set<String> getAcceptedUsers()
	{
		return acceptedUsers;
	}

	public void setAcceptedUsers(Set<String> acceptedUsers)
	{
		this.acceptedUsers = acceptedUsers;
	}

	public void addAccepted(String userId)
	{
		acceptedUsers.add(userId);
	}

	public String getAssignedTo()
	{
		return assignedTo;
	}

	public void setAssignedTo(String assignedTo)
	{
		this.assignedTo = assignedTo;
	}

	public Date getDateDue()
	{
		return dateDue;
	}

	public void setDateDue(Date dateDue)
	{
		this.dateDue = dateDue;
	}
}
