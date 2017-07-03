/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.item.standard.operations.workflow;

import java.io.IOException;

import com.dytech.edge.exceptions.WorkflowException;
import com.google.common.base.Throwables;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.filesystem.WorkflowMessageFile;
import com.tle.core.item.standard.workflow.nodes.TaskStatus;
import com.tle.core.notification.beans.Notification;
import com.tle.core.security.impl.SecureInModeration;

/**
 * @author jmaginnis
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
@SecureInModeration
public final class RejectOperation extends SpecificTaskOperation // NOSONAR
{
	private final String msg;
	private final String tostep;
	private final String messageUuid;

	@AssistedInject
	private RejectOperation(@Assisted("taskId") String taskId, @Assisted("comment") String msg,
		@Assisted("step") @Nullable String tostep, @Assisted("messageUuid") @Nullable String messageUuid)
	{
		super(taskId);
		this.msg = msg;
		this.tostep = tostep;
		this.messageUuid = messageUuid;
	}

	@SuppressWarnings("nls")
	@Override
	public boolean execute()
	{
		checkWeCanModerate();
		ModerationStatus modstatus = getModerationStatus();
		modstatus.setLastAction(params.getDateNow());

		TaskStatus status = getTaskStatus();
		params.setCause(status.getBean());
		HistoryEvent reject = createHistory(Type.rejected);
		reject.setComment(msg);
		setToStepFromTask(reject, tostep);
		setStepFromTask(reject);
		addMessage(WorkflowMessage.TYPE_REJECT, msg, messageUuid);

		if( Check.isEmpty(tostep) )
		{
			Item item = getItem();
			setState(ItemStatus.REJECTED);
			modstatus.setRejectedMessage(msg);
			modstatus.setRejectedBy(getUserId());
			modstatus.setRejectedStep(status.getId());
			exitTasksForItem();
			item.setModerating(false);
			removeModerationNotifications();
			addNotifications(item.getItemId(), getAllOwnerIds(), Notification.REASON_REJECTED, false);
		}
		else
		{
			WorkflowNode parentNode = status.getRejectNode(tostep);
			if( parentNode != null )
			{
				reenter(parentNode);
			}
			else
			{
				throw new WorkflowException("Rejection step is not a parent");
			}
		}
		updateModeration();

		try
		{
			fileSystemService.commitFiles(new StagingFile(messageUuid), new WorkflowMessageFile(messageUuid));
		}
		catch( IOException ex )
		{
			throw Throwables.propagate(ex);
		}

		return true;
	}
}
