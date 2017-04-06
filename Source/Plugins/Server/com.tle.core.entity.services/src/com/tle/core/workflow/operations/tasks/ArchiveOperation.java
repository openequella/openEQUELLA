/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.workflow.operations.tasks;

import com.tle.beans.item.ItemStatus;
import com.tle.common.security.SecurityConstants;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureNotInModeration;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

@SecureOnCall(priv = SecurityConstants.ARCHIVE_ITEM)
@SecureNotInModeration
@SecureItemStatus(ItemStatus.LIVE)
public class ArchiveOperation extends AbstractWorkflowOperation
{
	@Override
	public boolean execute()
	{
		setState(ItemStatus.ARCHIVED);
		return true;
	}
}
