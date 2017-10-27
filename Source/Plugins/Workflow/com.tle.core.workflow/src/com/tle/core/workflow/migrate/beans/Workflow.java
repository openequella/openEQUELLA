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

package com.tle.core.workflow.migrate.beans;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import org.hibernate.annotations.AccessType;

import com.tle.core.workflow.migrate.beans.node.WorkflowNode;

@Entity(name = "Workflow")
@AccessType("field")
public class Workflow
{
	private boolean movelive;

	@Id
	public long id;

	@Lob
	public String root;

	@OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL)
	private Set<WorkflowNode> nodes;

	public Workflow()
	{
		super();
	}

	public boolean isMovelive()
	{
		return movelive;
	}

	public void setMovelive(boolean movelive)
	{
		this.movelive = movelive;
	}

	public Set<WorkflowNode> getNodes()
	{
		return nodes;
	}

	public void setNodes(Set<WorkflowNode> nodes)
	{
		this.nodes = nodes;
	}

	public Map<String, WorkflowNode> getAllTasksAsMap()
	{
		Map<String, WorkflowNode> allNodes = new HashMap<String, WorkflowNode>();
		for( WorkflowNode node : nodes )
		{
			allNodes.put(node.getUuid(), node);
		}
		return allNodes;
	}
}
