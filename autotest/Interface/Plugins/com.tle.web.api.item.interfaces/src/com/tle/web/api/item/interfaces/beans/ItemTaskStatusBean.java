package com.tle.web.api.item.interfaces.beans;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tle.common.interfaces.I18NString;
import com.tle.web.api.interfaces.beans.UserBean;

public class ItemTaskStatusBean extends ItemNodeStatusBean
{
	private I18NString description;
	private Date started;
	private Date due;
	private boolean overdue;
	private int priority;
	private ItemNodeStatusBean cause;
	private UserBean assignedTo;

	private List<String> acceptedUsers;

	@JsonCreator
	public ItemTaskStatusBean(@JsonProperty("uuid") String uuid)
	{
		super(uuid);
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

	public boolean isOverdue()
	{
		return overdue;
	}

	public void setOverdue(boolean overdue)
	{
		this.overdue = overdue;
	}

	public int getPriority()
	{
		return priority;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	public UserBean getAssignedTo()
	{
		return assignedTo;
	}

	public void setAssignedTo(UserBean assignedTo)
	{
		this.assignedTo = assignedTo;
	}

	public List<String> getAcceptedUsers()
	{
		return acceptedUsers;
	}

	public void setAcceptedUsers(List<String> acceptedUsers)
	{
		this.acceptedUsers = acceptedUsers;
	}

	public I18NString getDescription()
	{
		return description;
	}

	public void setDescription(I18NString description)
	{
		this.description = description;
	}

	public ItemNodeStatusBean getCause()
	{
		return cause;
	}

	public void setCause(ItemNodeStatusBean cause)
	{
		this.cause = cause;
	}

}
