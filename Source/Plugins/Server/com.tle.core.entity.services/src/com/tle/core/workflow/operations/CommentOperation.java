/*
 * Created on Apr 21, 2005 For "The Learning Edge"
 */
package com.tle.core.workflow.operations;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.core.security.impl.SecureInModeration;
import com.tle.core.workflow.operations.tasks.SpecificTaskOperation;

/**
 * @author jmaginnis
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
@SecureInModeration
public final class CommentOperation extends SpecificTaskOperation // NOSONAR
{
	private final String msg;

	@AssistedInject
	private CommentOperation(@Assisted("taskId") String taskId, @Assisted("comment") String msg)
	{
		super(taskId);
		this.msg = msg;
	}

	@Override
	public boolean execute()
	{
		HistoryEvent comment = createHistory(Type.comment);
		comment.setComment(msg);
		setStepFromTask(comment);
		addMessage(WorkflowMessage.TYPE_COMMENT, msg);
		getModerationStatus().setLastAction(params.getDateNow());
		return true;
	}
}
