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
