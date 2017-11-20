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

package com.tle.beans.workflow;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.tle.beans.entity.LanguageBundle;
import com.tle.common.Check;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.node.WorkflowItem;

/**
 * @author jmaginnis
 */
public class WorkflowStep implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String uuid;
	private LanguageBundle name;
	private LanguageBundle description;
	private WorkflowItemStatus status;
	private Collection<String> toModerate;
	private final Collection<String> rolesToModerate = new HashSet<String>();
	private List<WorkflowStep> rejectPoints;
	private boolean approved;
	private boolean unanimous;

	public WorkflowStep(WorkflowItem task, WorkflowItemStatus status)
	{
		uuid = task.getUuid();
		name = task.getDisplayName();
		description = task.getDescription();
		if( !Check.isEmpty(task.getRoles()) )
		{
			rolesToModerate.addAll(task.getRoles());
		}
		this.status = status;
		unanimous = task.isUnanimousacceptance();
	}

	public WorkflowStep(String uuid, LanguageBundle name)
	{
		this.uuid = uuid;
		this.name = name;
	}

	public String getUuid()
	{
		return uuid;
	}

	public LanguageBundle getDisplayName()
	{
		return name;
	}

	public LanguageBundle getDescription()
	{
		return description;
	}

	public Collection<String> getToModerate()
	{
		return toModerate;
	}

	public void setToModerate(Collection<String> toModerate)
	{
		this.toModerate = toModerate;
	}

	public boolean isApproved()
	{
		return approved;
	}

	public void setApproved(boolean approved)
	{
		this.approved = approved;
	}

	public LanguageBundle getName()
	{
		return name;
	}

	public void setName(LanguageBundle name)
	{
		this.name = name;
	}

	public WorkflowItemStatus getStatus()
	{
		return status;
	}

	public void setStatus(WorkflowItemStatus status)
	{
		this.status = status;
	}

	public boolean isUnanimous()
	{
		return unanimous;
	}

	public void setUnanimous(boolean unanimous)
	{
		this.unanimous = unanimous;
	}

	public List<WorkflowStep> getRejectPoints()
	{
		return rejectPoints;
	}

	public void setRejectPoints(List<WorkflowStep> rejectPoints)
	{
		this.rejectPoints = rejectPoints;
	}

	public String getAssignedTo()
	{
		return status.getAssignedTo();
	}

	public Collection<String> getRolesToModerate()
	{
		return rolesToModerate;
	}
}
