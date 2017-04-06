package com.tle.common.security;

import java.io.Serializable;

public class WorkflowTaskTarget implements Serializable
{
	private static final long serialVersionUID = 1L;

	private long workflowId;
	private final String taskId;

	public WorkflowTaskTarget(long workflowId, String taskId)
	{
		this.workflowId = workflowId;
		this.taskId = taskId;
	}

	public String getTaskId()
	{
		return taskId;
	}

	public long getWorkflowId()
	{
		return workflowId;
	}

	public void setWorkflowId(long workflowId)
	{
		this.workflowId = workflowId;
	}

	public String getTarget()
	{
		return SecurityConstants.TARGET_WORKFLOW_TASK + ':' + workflowId + ':' + taskId;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( !(obj instanceof WorkflowTaskTarget) )
		{
			return false;
		}
		WorkflowTaskTarget ttarget = (WorkflowTaskTarget) obj;
		return workflowId == ttarget.workflowId && taskId.equals(ttarget.taskId);
	}

	@Override
	public int hashCode()
	{
		return (int) (workflowId ^ taskId.hashCode());
	}
}
