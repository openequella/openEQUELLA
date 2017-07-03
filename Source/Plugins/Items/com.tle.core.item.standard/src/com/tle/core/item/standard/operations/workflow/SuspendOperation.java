/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.item.standard.operations.workflow;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ModerationStatus;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;

/**
 * @author jmaginnis
 */
@SecureOnCall(priv = "SUSPEND_ITEM")
@SecureItemStatus(value = {ItemStatus.SUSPENDED, ItemStatus.PERSONAL}, not = true)
public class SuspendOperation extends TaskOperation
{
	@Override
	public boolean execute()
	{
		ModerationStatus modStatus = getModerationStatus();
		Item item = getItem();
		modStatus.setResumeStatus(getItemStatus());
		modStatus.setResumeModerating(item.isModerating());
		setState(ItemStatus.SUSPENDED);
		exitTasksForItem();
		item.setModerating(false);
		return true;
	}
}
