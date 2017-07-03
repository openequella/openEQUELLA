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

import com.tle.core.workflow.migrate.beans.node.FakeWorkflowNode;

@Entity(name = "Workflow")
@AccessType("field")
public class FakeWorkflow
{
	private boolean movelive;

	@Id
	public long id;

	@Lob
	public String root;

	@OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL)
	private Set<FakeWorkflowNode> nodes;

	public FakeWorkflow()
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

	public Set<FakeWorkflowNode> getNodes()
	{
		return nodes;
	}

	public void setNodes(Set<FakeWorkflowNode> nodes)
	{
		this.nodes = nodes;
	}

	public Map<String, FakeWorkflowNode> getAllTasksAsMap()
	{
		Map<String, FakeWorkflowNode> allNodes = new HashMap<String, FakeWorkflowNode>();
		for( FakeWorkflowNode node : nodes )
		{
			allNodes.put(node.getUuid(), node);
		}
		return allNodes;
	}
}
