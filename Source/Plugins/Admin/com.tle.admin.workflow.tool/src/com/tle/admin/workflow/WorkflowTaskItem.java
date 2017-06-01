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
