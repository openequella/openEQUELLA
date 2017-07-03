package com.tle.web.bulk.workflowtask.operations;

import java.io.IOException;
import java.util.UUID;

import com.google.common.base.Throwables;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ItemTaskId;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.filesystem.WorkflowMessageFile;
import com.tle.core.item.standard.workflow.nodes.TaskStatus;
import com.tle.core.notification.beans.Notification;

public class TaskRejectOperation extends AbstractBulkTaskOperation
{
	private final String message;
	private final String stagingFolderUuid;
	// private final boolean rejectAllUsers;

	@AssistedInject
	public TaskRejectOperation(@Assisted("message") String message,
		@Assisted("stagingFolderUuid") String stagingFolderUuid, @Assisted("rejectAllUsers") boolean rejectAllUsers)
	{
		this.message = message;
		this.stagingFolderUuid = stagingFolderUuid;
		//this.rejectAllUsers = rejectAllUsers;
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

		if( status != null )
		{
			String messageUuid = UUID.randomUUID().toString();
			StagingFile stagingFolder = new StagingFile(stagingFolderUuid);

			try
			{
				fileSystemService.saveFiles(stagingFolder, new WorkflowMessageFile(messageUuid));
			}
			catch( IOException ex )
			{
				throw Throwables.propagate(ex);
			}

			addMessage(taskId.getTaskId(), WorkflowMessage.TYPE_REJECT, message, messageUuid);

			WorkflowItemStatus bean = (WorkflowItemStatus) status.getBean();
			WorkflowItem workflowItem = (WorkflowItem) status.getWorkflowNode();

			// Does this even make sense??
			/*
			 * final Set<String> acceptedUsers = bean.getAcceptedUsers(); if(
			 * rejectAllUsers ) { // EQ-2544 a bulk approval/reject should move
			 * the item onward, even // when Unanimous=True.
			 * acceptedUsers.clear();
			 * acceptedUsers.addAll(getUsersToModerate(workflowItem)); } else {
			 * acceptedUsers.add(CurrentUser.getUserID()); }
			 */

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
		}
		updateModeration();
		return true;
	}
}
