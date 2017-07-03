package com.tle.core.item.standard.operations.workflow;

import com.dytech.edge.exceptions.WorkflowException;
import com.tle.beans.item.HistoryEvent;
import com.tle.core.item.standard.workflow.nodes.TaskStatus;

public abstract class SpecificTaskOperation extends TaskOperation
{
	private String taskId;

	public SpecificTaskOperation(String taskId)
	{
		this.taskId = taskId;
	}

	public TaskStatus getTaskStatus()
	{
		return (TaskStatus) getNodeStatus(taskId);
	}

	@SuppressWarnings("nls")
	public void checkWeCanModerate()
	{
		if( !getTaskStatus().canCurrentUserModerate(this) )
		{
			throw new WorkflowException("Current user can't moderate this step");
		}
	}

	protected void addMessage(char type, String message)
	{
		addMessage(type, message, null);
	}

	protected void addMessage(char type, String message, String uuid)
	{
		addMessage(taskId, type, message, uuid);
	}

	protected void setStepFromTask(HistoryEvent event)
	{
		setStepFromTask(event, taskId);
	}

}
