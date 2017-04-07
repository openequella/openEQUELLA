package com.tle.web.api.item.interfaces.beans;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class ItemNodeStatusExportBean
{
	private String uuid;
	private String status;
	private Date started;
	private Date due;
	@JsonInclude(Include.NON_DEFAULT)
	private int causeIndex = -1;
	private String assignedTo;
	private Set<String> acceptedUsers;
	private List<ItemNodeStatusMessageBean> comments;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public Date getStarted()
	{
		return started;
	}

	public void setStarted(Date started)
	{
		this.started = started;
	}

	public Date getDue()
	{
		return due;
	}

	public void setDue(Date due)
	{
		this.due = due;
	}

	public int getCauseIndex()
	{
		return causeIndex;
	}

	public void setCauseIndex(int causeIndex)
	{
		this.causeIndex = causeIndex;
	}

	public String getAssignedTo()
	{
		return assignedTo;
	}

	public void setAssignedTo(String assignedTo)
	{
		this.assignedTo = assignedTo;
	}

	public Set<String> getAcceptedUsers()
	{
		return acceptedUsers;
	}

	public void setAcceptedUsers(Set<String> acceptedUsers)
	{
		this.acceptedUsers = acceptedUsers;
	}

	public List<ItemNodeStatusMessageBean> getComments()
	{
		return comments;
	}

	public void setComments(List<ItemNodeStatusMessageBean> comments)
	{
		this.comments = comments;
	}

}