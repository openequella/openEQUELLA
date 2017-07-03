/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.item.standard.operations.workflow;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.core.security.impl.SecureItemStatus;

@SecureItemStatus(ItemStatus.LIVE)
public class InsecureArchiveOperation extends TaskOperation
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
