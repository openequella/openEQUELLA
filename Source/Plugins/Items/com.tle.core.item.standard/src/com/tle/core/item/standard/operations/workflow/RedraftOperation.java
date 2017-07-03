/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.item.standard.operations.workflow;

import com.tle.beans.item.ItemStatus;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;

/**
 * @author jmaginnis
 */
@SecureOnCall(priv = "REDRAFT_ITEM")
@SecureItemStatus(not = true, value = {ItemStatus.DRAFT, ItemStatus.PERSONAL})
public class RedraftOperation extends TaskOperation
{
	@Override
	public boolean execute()
	{
		exitTasksForItem();
		params.clearAllStatuses();
		removeModerationNotifications();
		getItem().setModerating(false);
		setState(ItemStatus.DRAFT);
		updateModeration();
		return true;
	}
}
