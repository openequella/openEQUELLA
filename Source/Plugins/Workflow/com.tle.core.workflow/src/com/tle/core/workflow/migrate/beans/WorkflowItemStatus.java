/*
 * Created on Aug 25, 2005
 */
package com.tle.core.workflow.migrate.beans;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;

import com.tle.core.workflow.migrate.beans.node.WorkflowNode;

@Entity(name = "WorkflowItemStatus")
@AccessType("field")
@DiscriminatorValue("task")
public class WorkflowItemStatus extends WorkflowNodeStatus
{
	private static final long serialVersionUID = 1L;

	private Date dateDue;
	@Column(length = 40)
	private String assignedTo;

	@ElementCollection
	@JoinTable(name = "WorkflowNodeStatusAccepted", joinColumns = @JoinColumn(name = "workflow_node_status_id") )
	@Column(name = "`user`", length = 255)
	private Set<String> acceptedUsers = new HashSet<String>();

	// To delete
	@Lob
	@Column(name = "acceptedUsers")
	public String oldAccepted;

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
