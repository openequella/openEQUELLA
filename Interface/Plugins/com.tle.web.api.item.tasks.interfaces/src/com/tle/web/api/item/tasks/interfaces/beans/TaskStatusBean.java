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

package com.tle.web.api.item.tasks.interfaces.beans;

import java.util.Collection;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.item.interfaces.beans.ItemBean;

@XmlRootElement
public class TaskStatusBean extends AbstractExtendableBean
{
	private TaskBean task;
	private ItemBean item;
	private Collection<UserBean> acceptedUsers;
	private UserBean assignedTo;
	private Date dueDate;
	private Date startDate;
	private boolean overdue;

	public ItemBean getItem()
	{
		return item;
	}

	public void setItem(ItemBean item)
	{
		this.item = item;
	}

	public TaskBean getTask()
	{
		return task;
	}

	public void setTask(TaskBean task)
	{
		this.task = task;
	}

	public Collection<UserBean> getAcceptedUsers()
	{
		return acceptedUsers;
	}

	public void setAcceptedUsers(Collection<UserBean> acceptedUsers)
	{
		this.acceptedUsers = acceptedUsers;
	}

	public UserBean getAssignedTo()
	{
		return assignedTo;
	}

	public void setAssignedTo(UserBean assignedTo)
	{
		this.assignedTo = assignedTo;
	}

	public Date getDueDate()
	{
		return dueDate;
	}

	public void setDueDate(Date dueDate)
	{
		this.dueDate = dueDate;
	}

	public Date getStartDate()
	{
		return startDate;
	}

	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	public boolean isOverdue()
	{
		return overdue;
	}

	public void setOverdue(boolean overdue)
	{
		this.overdue = overdue;
	}
}
