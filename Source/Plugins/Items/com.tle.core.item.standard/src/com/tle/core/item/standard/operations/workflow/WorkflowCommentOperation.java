/*
 * Created on Apr 21, 2005 For "The Learning Edge"
 */
package com.tle.core.item.standard.operations.workflow;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.core.security.impl.SecureInModeration;

/**
 * @author jmaginnis
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
@SecureInModeration
public final class WorkflowCommentOperation extends SpecificTaskOperation // NOSONAR
{
	private final String msg;
	private final String messageUuid;

	@AssistedInject
	private WorkflowCommentOperation(@Assisted("taskId") String taskId, @Assisted("comment") String msg,
		@Assisted("messageUuid") @Nullable String messageUuid)
	{
		super(taskId);
		this.msg = msg;
		this.messageUuid = messageUuid;
	}

	@Override
	public boolean execute()
	{
		HistoryEvent comment = createHistory(Type.comment);
		comment.setComment(msg);
		setStepFromTask(comment);
		addMessage(WorkflowMessage.TYPE_COMMENT, msg, messageUuid);
		getModerationStatus().setLastAction(params.getDateNow());
		return true;
	}
}
