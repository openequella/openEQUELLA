/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.item.standard.operations;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.item.standard.operations.workflow.TaskOperation;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;

/**
 * @author jmaginnis
 */
@SecureItemStatus(ItemStatus.DELETED)
@SecureOnCall(priv = "DELETE_ITEM")
public class RestoreDeletedOperation extends TaskOperation
{
	private String lastRestoredName;

	public String getLastRestoredName()
	{
		return lastRestoredName;
	}

	@Override
	public boolean execute()
	{
		boolean modified = false;

		ModerationStatus moderationStatus = getModerationStatus();
		ItemStatus deletedStatus = moderationStatus.getDeletedStatus();
		if( deletedStatus == null )
		{
			deletedStatus = ItemStatus.LIVE;
		}
		Item item = getItem();
		item.setModerating(moderationStatus.isDeletedModerating());
		setState(deletedStatus);
		modified = true;
		lastRestoredName = CurrentLocale.get(item.getName());
		restoreTasksForItem();
		return modified;
	}
}
