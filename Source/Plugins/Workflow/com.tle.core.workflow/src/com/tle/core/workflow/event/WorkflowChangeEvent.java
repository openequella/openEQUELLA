package com.tle.core.workflow.event;

import java.util.Set;

import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.workflow.event.listener.WorkflowChangeListener;

public class WorkflowChangeEvent extends ApplicationEvent<WorkflowChangeListener>
{
	private static final long serialVersionUID = 1L;

	private final boolean delete;
	private final Set<WorkflowNode> nodes;
	private final long workflowId;

	public WorkflowChangeEvent(long workflowId, Set<WorkflowNode> nodes, boolean delete)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.workflowId = workflowId;
		this.nodes = nodes;
		this.delete = delete;
	}

	@Override
	public void postEvent(WorkflowChangeListener listener)
	{
		listener.workflowChange(this);
	}

	@Override
	public Class<WorkflowChangeListener> getListener()
	{
		return WorkflowChangeListener.class;
	}

	public boolean isDelete()
	{
		return delete;
	}

	public Set<WorkflowNode> getNodes()
	{
		return nodes;
	}

	public long getWorkflowId()
	{
		return workflowId;
	}
}
