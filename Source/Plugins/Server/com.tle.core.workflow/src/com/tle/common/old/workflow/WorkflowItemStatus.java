/*
 * Created on Aug 25, 2005
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
