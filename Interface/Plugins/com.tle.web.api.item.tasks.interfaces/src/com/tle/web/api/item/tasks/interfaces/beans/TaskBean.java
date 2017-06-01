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

import com.tle.common.interfaces.BaseEntityReference;
import com.tle.common.interfaces.I18NString;

public class TaskBean
{
	private String uuid;
	private I18NString name;
	private I18NString description;
	private int priority;
	private boolean unanimous;
	private BaseEntityReference workflow;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public int getPriority()
	{
		return priority;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	public boolean isUnanimous()
	{
		return unanimous;
	}

	public void setUnanimous(boolean unanimous)
	{
		this.unanimous = unanimous;
	}

	public I18NString getName()
	{
		return name;
	}

	public void setName(I18NString name)
	{
		this.name = name;
	}

	public I18NString getDescription()
	{
		return description;
	}

	public void setDescription(I18NString description)
	{
		this.description = description;
	}

	public BaseEntityReference getWorkflow()
	{
		return workflow;
	}

	public void setWorkflow(BaseEntityReference workflow)
	{
		this.workflow = workflow;
	}
}
