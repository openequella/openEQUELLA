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

package com.tle.common.old.workflow.node;

import java.util.List;
import java.util.Set;

import com.tle.beans.entity.LanguageBundle;

public class WorkflowItem extends WorkflowNode
{
	private static final long serialVersionUID = 1;

	private List<String> users;
	private List<String> groups;
	private List<String> roles;

	// This field is unused, but retained for XStream compatibility
	@SuppressWarnings("all")
	private String task;

	private LanguageBundle description;
	private boolean unanimousacceptance;
	private boolean escalate;
	private int escalationdays;
	private boolean movelive;
	private boolean allowEditing;
	private boolean rejectPoint;
	// Auto-assign based on steps
	private Set<String> autoAssigns;
	// Auto-assign based on user ID in this metadata path
	private String autoAssignFromMetadataPath;
	private String autoAssignFromMetadataSchemaUuid;
	private String userPath;
	private String userSchemaUuid;

	public WorkflowItem(final LanguageBundle name)
	{
		super(name);
		escalationdays = 0;
		task = ""; //$NON-NLS-1$
	}

	public WorkflowItem()
	{
		this(null);
	}

	@Override
	protected int getDefaultType()
	{
		return WorkflowNode.ITEM_TYPE;
	}

	public LanguageBundle getDisplayName()
	{
		return name;
	}

	public LanguageBundle getDescription()
	{
		return description;
	}

	public void setDescription(final LanguageBundle description)
	{
		this.description = description;
	}

	public boolean isEscalate()
	{
		return escalate;
	}

	public void setEscalate(final boolean escalate)
	{
		this.escalate = escalate;
	}

	public int getEscalationdays()
	{
		return escalationdays;
	}

	public void setEscalationdays(final int escalationdays)
	{
		this.escalationdays = escalationdays;
	}

	public boolean isMovelive()
	{
		return movelive;
	}

	public void setMovelive(final boolean movelive)
	{
		this.movelive = movelive;
	}

	public boolean isUnanimousacceptance()
	{
		return unanimousacceptance;
	}

	public void setUnanimousacceptance(final boolean unanimousacceptance)
	{
		this.unanimousacceptance = unanimousacceptance;
	}

	public List<String> getGroups()
	{
		return groups;
	}

	public void setGroups(final List<String> group)
	{
		this.groups = group;
	}

	public List<String> getUsers()
	{
		return users;
	}

	public void setUsers(final List<String> user)
	{
		this.users = user;
	}

	public List<String> getRoles()
	{
		return roles;
	}

	public void setRoles(final List<String> roles)
	{
		this.roles = roles;
	}

	public Set<String> getAutoAssigns()
	{
		return autoAssigns;
	}

	public void setAutoAssigns(final Set<String> autoAssigns)
	{
		this.autoAssigns = autoAssigns;
	}

	public String getAutoAssignFromMetadataPath()
	{
		return autoAssignFromMetadataPath;
	}

	public void setAutoAssignFromMetadataPath(final String autoAssignFromMetadata)
	{
		this.autoAssignFromMetadataPath = autoAssignFromMetadata;
	}

	public String getAutoAssignFromMetadataSchemaUuid()
	{
		return autoAssignFromMetadataSchemaUuid;
	}

	public void setAutoAssignFromMetadataSchemaUuid(final String autoAssignFromMetadataSchemaUuid)
	{
		this.autoAssignFromMetadataSchemaUuid = autoAssignFromMetadataSchemaUuid;
	}

	public boolean isAllowEditing()
	{
		return allowEditing;
	}

	public void setAllowEditing(final boolean allowEditing)
	{
		this.allowEditing = allowEditing;
	}

	public boolean isRejectPoint()
	{
		return rejectPoint;
	}

	public void setRejectPoint(final boolean rejectPoint)
	{
		this.rejectPoint = rejectPoint;
	}

	public String getUserPath()
	{
		return userPath;
	}

	public void setUserPath(String userPath)
	{
		this.userPath = userPath;
	}

	public String getUserSchemaUuid()
	{
		return userSchemaUuid;
	}

	public void setUserSchemaUuid(String userSchemaUuid)
	{
		this.userSchemaUuid = userSchemaUuid;
	}

}
