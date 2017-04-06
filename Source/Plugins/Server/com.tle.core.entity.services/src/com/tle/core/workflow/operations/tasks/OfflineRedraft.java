package com.tle.core.workflow.operations.tasks;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

@SecureItemStatus(not = true, value = {ItemStatus.DRAFT, ItemStatus.PERSONAL})
public class OfflineRedraft extends AbstractWorkflowOperation
{
	@Override
	public boolean execute()
	{
		Item item = getItem();
		item.setStatus(ItemStatus.DRAFT);
		item.setModerating(false);
		params.setUpdateSecurity(true);
		return false;
	}

}
