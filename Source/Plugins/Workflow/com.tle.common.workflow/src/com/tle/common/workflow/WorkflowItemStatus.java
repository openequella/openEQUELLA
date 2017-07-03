/*
 * Created on Aug 25, 2005
 */
package com.tle.common.workflow;

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

import com.tle.common.workflow.node.WorkflowNode;

@Entity
@AccessType("field")
@DiscriminatorValue("task")
public class WorkflowItemStatus extends WorkflowNodeStatus
{
	private static final long serialVersionUID = 1L;

	@Index(name = "datedue_idx")
	private Date dateDue;
	private Date started;
	private boolean overdue;
	@Column(length = 40)
	private String assignedTo;
	@ElementCollection
	@JoinTable(name = "WorkflowNodeStatusAccepted", joinColumns = @JoinColumn(name = "workflow_node_status_id") )
	@Column(name = "`user`", length = 255)
	private Set<String> acceptedUsers = new HashSet<String>();
	@ManyToOne
	@Index(name = "cause_idx")
	private WorkflowNodeStatus cause;

	public WorkflowItemStatus()
	{
		super();
	}

	public WorkflowItemStatus(WorkflowNode node, WorkflowNodeStatus cause)
	{
		super(node);
		this.cause = cause;
	}

	public Set<String> getAcceptedUsers()
	{
		return acceptedUsers;
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

	public WorkflowNodeStatus getCause()
	{
		return cause;
	}

	public void setCause(WorkflowNodeStatus cause)
	{
		this.cause = cause;
	}

	@Override
	public void archive()
	{
		super.archive();
		acceptedUsers.clear();
		dateDue = null;
	}

	public boolean isOverdue()
	{
		return overdue;
	}

	public void setOverdue(boolean overdue)
	{
		this.overdue = overdue;
	}

	public Date getStarted()
	{
		return started;
	}

	public void setStarted(Date started)
	{
		this.started = started;
	}
}
