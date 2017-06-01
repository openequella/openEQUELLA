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

package com.tle.common.workflow.node;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.entity.LanguageBundle;
import com.tle.common.Check;

@Entity
@AccessType("field")
@DiscriminatorValue("t")
public class WorkflowItem extends WorkflowNode
{
	public enum MoveLive
	{
		NO, ARRIVAL, ACCEPTED
	}

	public enum AutoAction
	{
		NONE, ACCEPT, REJECT
	}

	public enum Priority
	{
		LOW(10), NORMAL(20), HIGH(30);

		private int intValue;

		Priority(int intValue)
		{
			this.intValue = intValue;
		}

		public int intValue()
		{
			return intValue;
		}
	}

	private static final long serialVersionUID = 1;

	@ElementCollection
	@Column(name = "`user`", length = 255)
	@CollectionTable(name = "workflow_node_users", joinColumns = @JoinColumn(name = "workflow_node_id"))
	private Set<String> users;
	@ElementCollection
	@Column(name = "`group`", length = 255)
	@CollectionTable(name = "workflow_node_groups", joinColumns = @JoinColumn(name = "workflow_node_id"))
	private Set<String> groups;
	@ElementCollection
	@Column(name = "role", length = 255)
	@CollectionTable(name = "workflow_node_roles", joinColumns = @JoinColumn(name = "workflow_node_id"))
	private Set<String> roles;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Index(name = "workflowNodeDesc")
	private LanguageBundle description;
	private boolean unanimousacceptance;
	private boolean escalate;
	private int escalationdays;
	private boolean allowEditing;
	private boolean movelive;
	private boolean moveliveAccept;
	private int priority;
	private Boolean autoAction;
	private int actionDays;

	// Auto-assign based on steps
	@ElementCollection
	@Column(name = "`task`", length = 40)
	@CollectionTable(name = "workflow_node_auto_assigns", joinColumns = @JoinColumn(name = "workflow_node_id"))
	private Set<String> autoAssigns;
	// Auto-assign based on user ID in this metadata path
	@Column(length = 512)
	private String autoAssignNode;
	@Column(length = 40)
	private String autoAssignSchemaUuid;
	@Column(length = 512)
	private String userPath;
	@Column(length = 40)
	private String userSchemaUuid;
	@Column(length = 512)
	private String dueDatePath;
	@Column(length = 40)
	private String dueDateSchemaUuid;

	public WorkflowItem(final LanguageBundle name)
	{
		super(name);
		escalationdays = 0;
	}

	public WorkflowItem()
	{
		this(null);
	}

	@Override
	public char getType()
	{
		return 't';
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

	public boolean isUnanimousacceptance()
	{
		return unanimousacceptance;
	}

	public void setUnanimousacceptance(final boolean unanimousacceptance)
	{
		this.unanimousacceptance = unanimousacceptance;
	}

	public Set<String> getGroups()
	{
		return groups;
	}

	public void setGroups(final Set<String> group)
	{
		this.groups = group;
	}

	public Set<String> getUsers()
	{
		return users;
	}

	public void setUsers(final Set<String> user)
	{
		this.users = user;
	}

	public Set<String> getRoles()
	{
		return roles;
	}

	public void setRoles(final Set<String> roles)
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

	public boolean isAllowEditing()
	{
		return allowEditing;
	}

	public void setAllowEditing(final boolean allowEditing)
	{
		this.allowEditing = allowEditing;
	}

	public String getAutoAssignNode()
	{
		return autoAssignNode;
	}

	public void setAutoAssignNode(String autoAssignNode)
	{
		this.autoAssignNode = autoAssignNode;
	}

	public String getAutoAssignSchemaUuid()
	{
		return autoAssignSchemaUuid;
	}

	public void setAutoAssignSchemaUuid(String autoAssignSchemaUuid)
	{
		this.autoAssignSchemaUuid = autoAssignSchemaUuid;
	}

	@Override
	public boolean canHaveSiblingRejectPoints()
	{
		return false;
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

	public boolean isModeratorsSpecified()
	{
		return (!Check.isEmpty(userPath) || !Check.isEmpty(users) || !Check.isEmpty(groups) || !Check.isEmpty(roles));
	}

	public int getPriority()
	{
		return priority;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	public AutoAction getAutoAction()
	{
		if( autoAction == null )
		{
			return AutoAction.NONE;
		}
		return autoAction ? AutoAction.ACCEPT : AutoAction.REJECT;
	}

	public void setAutoAction(AutoAction autoAction)
	{
		if( autoAction == AutoAction.NONE )
		{
			this.autoAction = null;
		}
		else
		{
			this.autoAction = autoAction == AutoAction.ACCEPT;
		}
	}

	public MoveLive getMovelive()
	{
		if( !movelive )
		{
			return MoveLive.NO;
		}
		return moveliveAccept ? MoveLive.ACCEPTED : MoveLive.ARRIVAL;
	}

	public void setMovelive(MoveLive movelive)
	{
		this.movelive = movelive != MoveLive.NO;
		this.moveliveAccept = (movelive == MoveLive.ACCEPTED);
	}

	public int getActionDays()
	{
		return actionDays;
	}

	public void setActionDays(int actionDays)
	{
		this.actionDays = actionDays;
	}

	public String getDueDatePath()
	{
		return dueDatePath;
	}

	public void setDueDatePath(String dueDatePath)
	{
		this.dueDatePath = dueDatePath;
	}

	public String getDueDateSchemaUuid()
	{
		return dueDateSchemaUuid;
	}

	public void setDueDateSchemaUuid(String dueDateSchemaUuid)
	{
		this.dueDateSchemaUuid = dueDateSchemaUuid;
	}

}
