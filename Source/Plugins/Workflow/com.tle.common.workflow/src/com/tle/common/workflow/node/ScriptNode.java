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
import javax.persistence.Lob;
import javax.persistence.OneToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.entity.LanguageBundle;
import com.tle.common.Check;
import com.tle.common.workflow.node.WorkflowItem.MoveLive;

@Entity(name = "WorkflowScript")
@AccessType("field")
@DiscriminatorValue("x")
public class ScriptNode extends WorkflowTreeNode
{
	private static final long serialVersionUID = 1;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Index(name = "workflowNodeDesc")
	private LanguageBundle description;
	private boolean movelive;
	private boolean moveliveAccept;
	private boolean proceedNext;
	@Lob
	private String script;
	
	@ElementCollection
	@Column(name = "`user`", length = 255)
	@CollectionTable(name = "wf_script_notify_completion_u", joinColumns = @JoinColumn(name = "workflow_node_id"))
	private Set<String> usersNotifyOnCompletion;
	@ElementCollection
	@Column(name = "`group`", length = 255)
	@CollectionTable(name = "wf_script_notify_completion_g", joinColumns = @JoinColumn(name = "workflow_node_id"))
	private Set<String> groupsNotifyOnCompletion;

	@ElementCollection
	@Column(name = "`user`", length = 255)
	@CollectionTable(name = "wf_script_notify_error_u", joinColumns = @JoinColumn(name = "workflow_node_id"))
	private Set<String> usersNotifyOnError;
	@ElementCollection
	@Column(name = "`group`", length = 255)
	@CollectionTable(name = "wf_script_notify_error_g", joinColumns = @JoinColumn(name = "workflow_node_id"))
	private Set<String> groupsNotifyOnError;

	private boolean notifyOnCompletion;
	@Column(name="notifyOnError")
	private boolean _notifyOnError;

	public ScriptNode(final LanguageBundle name)
	{
		super(name);
	}
	
	public ScriptNode()
	{
		super();
	}
	
	@Override
	public char getType()
	{
		return 'x';
	}

	@Override
	public boolean canAddChildren()
	{
		return false;
	}

	@Override
	public boolean canHaveSiblingRejectPoints()
	{
		return false;
	}
	
	public LanguageBundle getDescription()
	{
		return description;
	}

	public void setDescription(final LanguageBundle description)
	{
		this.description = description;
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

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}

	public boolean isProceedNext()
	{
		return proceedNext;
	}

	public void setProceedNext(final boolean proceedNext)
	{
		this.proceedNext = proceedNext;
	}

	public boolean isNotifyOnCompletion()
	{
		return notifyOnCompletion;
	}

	public void setNotifyOnCompletion(boolean notifyOnCompletion)
	{
		this.notifyOnCompletion = notifyOnCompletion;
	}

	public Set<String> getUsersNotifyOnCompletion()
	{
		return usersNotifyOnCompletion;
	}

	public void setUsersNotifyOnCompletion(Set<String> usersNotifyOnCompletion)
	{
		this.usersNotifyOnCompletion = usersNotifyOnCompletion;
	}

	public Set<String> getGroupsNotifyOnCompletion()
	{
		return groupsNotifyOnCompletion;
	}

	public void setGroupsNotifyOnCompletion(Set<String> groupsNotifyOnCompletion)
	{
		this.groupsNotifyOnCompletion = groupsNotifyOnCompletion;
	}

	public Set<String> getUsersNotifyOnError()
	{
		return usersNotifyOnError;
	}

	public void setUsersNotifyOnError(Set<String> usersNotifyOnError)
	{
		this.usersNotifyOnError = usersNotifyOnError;
	}

	public Set<String> getGroupsNotifyOnError()
	{
		return groupsNotifyOnError;
	}

	public void setGroupsNotifyOnError(Set<String> groupsNotifyOnError)
	{
		this.groupsNotifyOnError = groupsNotifyOnError;
	}

	public boolean isNotifyOnCompletionSpecified()
	{
		return (!Check.isEmpty(usersNotifyOnCompletion) || !Check.isEmpty(groupsNotifyOnCompletion));
	}

	public boolean isNotifyNoErrorSpecified()
	{
		return (!Check.isEmpty(usersNotifyOnError) || !Check.isEmpty(groupsNotifyOnError));
	}
}
