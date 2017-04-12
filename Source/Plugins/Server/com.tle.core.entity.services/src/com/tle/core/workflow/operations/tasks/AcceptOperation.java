/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.workflow.operations.tasks;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.common.Check;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.core.security.impl.SecureInModeration;
import com.tle.core.workflow.nodes.TaskStatus;

/**
 * @author jmaginnis
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
@SecureInModeration
public final class AcceptOperation extends SpecificTaskOperation // NOSONAR
{
	private final String message;

	@AssistedInject
	private AcceptOperation(@Assisted("taskId") String taskId, @Assisted("comment") @Nullable String message)
	{
		super(taskId);
		this.message = message;
	}

	@Override
	public boolean execute()
	{
		checkWeCanModerate();
		TaskStatus status = getTaskStatus();
		params.setCause(status.getBean());
		getModerationStatus().setLastAction(params.getDateNow());
		status.addAccepted(getUserId());
		HistoryEvent approved = createHistory(Type.approved);
		setStepFromTask(approved);
		if( !Check.isEmpty(message) )
		{
			approved.setComment(message);
			addMessage(WorkflowMessage.TYPE_ACCEPT, message);
		}
		status.update();
		updateModeration();
		return true;
	}

}
