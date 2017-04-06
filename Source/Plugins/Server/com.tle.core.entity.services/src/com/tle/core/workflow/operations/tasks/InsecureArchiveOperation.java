/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.workflow.operations.tasks;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.core.guice.Bind;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

@SecureItemStatus(ItemStatus.LIVE)
@Bind
public class InsecureArchiveOperation extends AbstractWorkflowOperation
{
	@Override
	public boolean execute()
	{
		Item item = getItem();
		boolean moderating = item.isModerating();
		getModerationStatus().setUnarchiveModerating(moderating);
		item.setModerating(false);
		setState(ItemStatus.ARCHIVED);
		if( moderating )
		{
			exitTasksForItem();
		}
		return true;
	}
}
