package com.tle.core.item.standard.operations.workflow;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;
import com.tle.core.security.impl.SecureItemStatus;

@SecureItemStatus(not = true, value = {ItemStatus.DRAFT, ItemStatus.PERSONAL})
public class OfflineRedraft extends AbstractStandardWorkflowOperation
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
