/*
 * Created on Jul 4, 2005
 */
package com.tle.admin.workflow;

import com.tle.common.NameValue;
import com.tle.common.workflow.node.WorkflowItem;

public class WorkflowTaskItem
{
	private WorkflowItem item;
	private NameValue user;
	private NameValue group;

	public WorkflowTaskItem()
	{
		super();
	}

	public WorkflowTaskItem(WorkflowItem item2)
	{
		this.item = item2;
	}

	public NameValue getGroup()
	{
		return group;
	}

	public void setGroup(NameValue group)
	{
		this.group = group;
	}

	public WorkflowItem getItem()
	{
		return item;
	}

	public void setItem(WorkflowItem item)
	{
		this.item = item;
	}

	public NameValue getUser()
	{
		return user;
	}

	public void setUser(NameValue user)
	{
		this.user = user;
	}
}
