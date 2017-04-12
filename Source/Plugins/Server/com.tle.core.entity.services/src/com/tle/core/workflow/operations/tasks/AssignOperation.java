package com.tle.core.workflow.operations.tasks;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.core.security.impl.SecureInModeration;
import com.tle.core.workflow.nodes.TaskStatus;

@SecureInModeration
public class AssignOperation extends SpecificTaskOperation
{
	@AssistedInject
	public AssignOperation(@Assisted String taskId)
	{
		super(taskId);
	}

	@Override
	public boolean execute()
	{
		TaskStatus status = getTaskStatus();
		String assignedTo = status.getAssignedTo();
		HistoryEvent event = createHistory(Type.assign);
		if( assignedTo != null && assignedTo.equals(getUserId()) )
		{
			status.setAssignedTo(null);
			event.setUser(null);
		}
		else
		{
			status.setAssignedTo(getUserId());
		}
		setStepFromTask(event);
		updateModeration();
		return true;
	}
}
