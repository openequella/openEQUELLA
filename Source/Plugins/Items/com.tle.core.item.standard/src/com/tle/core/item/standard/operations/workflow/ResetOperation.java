/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.item.standard.operations.workflow;

import com.tle.beans.item.ItemStatus;
import com.tle.core.security.impl.SecureItemStatus;

/**
 * @author jmaginnis
 */
@SecureItemStatus({ItemStatus.MODERATING, ItemStatus.REVIEW, ItemStatus.LIVE})
public class ResetOperation extends TaskOperation
{
	@Override
	public boolean execute()
	{
		setState(ItemStatus.MODERATING);
		resetWorkflow();
		updateModeration();
		return true;
	}
}
