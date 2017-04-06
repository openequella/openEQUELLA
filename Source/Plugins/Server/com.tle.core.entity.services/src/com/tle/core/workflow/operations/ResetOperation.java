/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.workflow.operations;

import com.tle.beans.item.ItemStatus;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.workflow.operations.tasks.TaskOperation;

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
