package com.tle.web.bulk.workflowtask.operations;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.*;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.item.standard.workflow.nodes.TaskStatus;
import com.tle.core.notification.beans.Notification;

public class TaskRejectOperation extends AbstractBulkTaskOperation
{
	private final String message;

	@AssistedInject
	public TaskRejectOperation(@Assisted("message") String message)
	{
		this.message = message;
	}

	@Override
	public boolean execute()
	{
		TaskStatus status = init();
		ItemTaskId taskId = getTaskId();

		ModerationStatus modstatus = getModerationStatus();
		modstatus.setLastAction(params.getDateNow());
		HistoryEvent reject = createHistory(Type.rejected);
		reject.setComment(message);
		setStepFromTask(reject, taskId.getTaskId());
		addMessage(taskId.getTaskId(), WorkflowMessage.TYPE_REJECT, message, null);

		WorkflowItemStatus bean = (WorkflowItemStatus) status.getBean();
		params.setCause(bean);

		// EQ-2546 Once rejected, the item moves to the closest previous
		// rejection point.
		WorkflowNode rejectNode = status.getClosestRejectNode();
		if( rejectNode != null )
		{
			setToStepFromTask(reject, rejectNode.getUuid());
			reenter(rejectNode);
		}
		else
		{
			setToStepFromTask(reject, null);
			Item item = getItem();
			setState(ItemStatus.REJECTED);
			modstatus.setRejectedMessage(message);
			modstatus.setRejectedBy(getUserId());
			modstatus.setRejectedStep(status.getId());
			exitTasksForItem();
			item.setModerating(false);
			removeModerationNotifications();
			addNotifications(item.getItemId(), getAllOwnerIds(), Notification.REASON_REJECTED, false);
		}
		updateModeration();
		return true;
	}
}
