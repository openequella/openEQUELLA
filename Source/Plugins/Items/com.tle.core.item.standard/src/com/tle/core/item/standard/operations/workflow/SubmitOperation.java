/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.item.standard.operations.workflow;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.core.security.impl.SecureItemStatus;

/**
 * @author jmaginnis
 */
@SecureItemStatus(ItemStatus.DRAFT)
public class SubmitOperation extends TaskOperation
{
	private String message;

	@AssistedInject
	public SubmitOperation(@Nullable @Assisted String message)
	{
		this.message = message;
	}

	@AssistedInject
	public SubmitOperation()
	{
		// no message;
	}

	@Override
	public boolean execute()
	{
		Workflow workflow = getWorkflow();
		if( workflow != null )
		{
			setState(ItemStatus.MODERATING);
		}
		// ...else, we don't have a workflow so let the resetWorkflow() call
		// make the status change. This will still put us in the "moderating"
		// status if we have a workflow but fall straight through to Live, but
		// that's fine.

		resetWorkflow();
		if( workflow != null && !Check.isEmpty(message) )
		{
			String taskId = workflow.getRoot().getUuid();
			HistoryEvent comment = createHistory(Type.comment);
			comment.setComment(message);
			setStepFromTask(comment, taskId);
			addMessage(taskId, WorkflowMessage.TYPE_SUBMIT, message, null);
		}
		getModerationStatus().setLastAction(params.getDateNow());
		updateModeration();
		return true;
	}
}
