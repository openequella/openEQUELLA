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

package com.tle.web.api.workflow.interfaces.beans;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;
import com.tle.common.interfaces.BaseEntityReference;
import com.tle.common.interfaces.I18NString;
import com.tle.common.interfaces.I18NStrings;

@JsonSerialize(include = Inclusion.NON_DEFAULT)
public class WorkflowNodeBean
{
	private String uuid;
	private I18NString name;
	private I18NStrings nameStrings;
	private char type;
	private boolean rejectPoint;
	private List<WorkflowNodeBean> nodes = new LinkedList<WorkflowNodeBean>();

	// WorkflowItem
	private Set<String> users;
	private Set<String> groups;
	private Set<String> roles;
	private I18NString description;
	private I18NStrings descriptionStrings;
	// WTF?
	private boolean unanimousacceptance;
	private boolean escalate;
	// WTF?
	private int escalationdays;
	private boolean allowEditing;
	private String moveLive;
	private String autoAction;
	private int priority;
	private int actionDays;
	private String userPath;
	private String dueDatePath;

	// DecisionNode
	private String script;
	private BaseEntityReference collection;

	public Set<String> getGroups()
	{
		return groups;
	}

	public void setGroups(Set<String> groups)
	{
		this.groups = groups;
	}

	public Set<String> getRoles()
	{
		return roles;
	}

	public void setRoles(Set<String> roles)
	{
		this.roles = roles;
	}

	public I18NString getDescription()
	{
		return description;
	}

	public void setDescription(I18NString description)
	{
		this.description = description;
	}

	public boolean isUnanimousacceptance()
	{
		return unanimousacceptance;
	}

	public void setUnanimousacceptance(boolean unanimousacceptance)
	{
		this.unanimousacceptance = unanimousacceptance;
	}

	public boolean isEscalate()
	{
		return escalate;
	}

	public void setEscalate(boolean escalate)
	{
		this.escalate = escalate;
	}

	public int getEscalationdays()
	{
		return escalationdays;
	}

	public void setEscalationdays(int escalationdays)
	{
		this.escalationdays = escalationdays;
	}

	public boolean isAllowEditing()
	{
		return allowEditing;
	}

	public void setAllowEditing(boolean allowEditing)
	{
		this.allowEditing = allowEditing;
	}

	public int getPriority()
	{
		return priority;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	public int getActionDays()
	{
		return actionDays;
	}

	public void setActionDays(int actionDays)
	{
		this.actionDays = actionDays;
	}

	public String getUserPath()
	{
		return userPath;
	}

	public void setUserPath(String userPath)
	{
		this.userPath = userPath;
	}

	public String getDueDatePath()
	{
		return dueDatePath;
	}

	public void setDueDatePath(String dueDatePath)
	{
		this.dueDatePath = dueDatePath;
	}

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}

	public BaseEntityReference getCollection()
	{
		return collection;
	}

	public void setCollection(BaseEntityReference collection)
	{
		this.collection = collection;
	}

	public char getType()
	{
		return type;
	}

	public void setType(char type)
	{
		this.type = type;
	}

	public boolean isRejectPoint()
	{
		return rejectPoint;
	}

	public void setRejectPoint(boolean rejectPoint)
	{
		this.rejectPoint = rejectPoint;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public I18NString getName()
	{
		return name;
	}

	public void setName(I18NString name)
	{
		this.name = name;
	}

	public I18NStrings getNameStrings()
	{
		return nameStrings;
	}

	public void setNameStrings(I18NStrings nameStrings)
	{
		this.nameStrings = nameStrings;
	}

	public List<WorkflowNodeBean> getNodes()
	{
		return nodes;
	}

	public void setNodes(List<WorkflowNodeBean> nodes)
	{
		this.nodes = nodes;
	}

	public Set<String> getUsers()
	{
		return users;
	}

	public void setUsers(Set<String> users)
	{
		this.users = users;
	}

	public I18NStrings getDescriptionStrings()
	{
		return descriptionStrings;
	}

	public void setDescriptionStrings(I18NStrings descriptionStrings)
	{
		this.descriptionStrings = descriptionStrings;
	}

	public String getMoveLive()
	{
		return moveLive;
	}

	public void setMoveLive(String moveLive)
	{
		this.moveLive = moveLive;
	}

	public String getAutoAction()
	{
		return autoAction;
	}

	public void setAutoAction(String autoAction)
	{
		this.autoAction = autoAction;
	}
}
