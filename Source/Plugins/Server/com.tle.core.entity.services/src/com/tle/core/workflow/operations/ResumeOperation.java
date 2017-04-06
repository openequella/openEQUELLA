/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.workflow.operations;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ModerationStatus;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;

/**
 * @author jmaginnis
 */
@SecureOnCall(priv = "SUSPEND_ITEM")
@SecureItemStatus(ItemStatus.SUSPENDED)
public class ResumeOperation extends AbstractWorkflowOperation
{
	protected ResumeOperation()
	{
		// please use guice
	}

	@Override
	public boolean execute()
	{
		ModerationStatus moderationStatus = getModerationStatus();
		Item item = getItem();
		ItemStatus resumeStatus = moderationStatus.getResumeStatus();
		if( resumeStatus == null )
		{
			resumeStatus = ItemStatus.LIVE;
		}
		item.setModerating(moderationStatus.isResumeModerating());
		setState(resumeStatus);
		restoreTasksForItem();
		return true;
	}
}
