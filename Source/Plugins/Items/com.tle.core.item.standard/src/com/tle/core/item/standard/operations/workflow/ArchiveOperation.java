/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.item.standard.operations.workflow;

import com.tle.beans.item.ItemStatus;
import com.tle.common.security.SecurityConstants;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureNotInModeration;
import com.tle.core.security.impl.SecureOnCall;

@SecureOnCall(priv = SecurityConstants.ARCHIVE_ITEM)
@SecureNotInModeration
@SecureItemStatus(ItemStatus.LIVE)
public class ArchiveOperation extends AbstractStandardWorkflowOperation
{
	@Override
	public boolean execute()
	{
		setState(ItemStatus.ARCHIVED);
		return true;
	}
}
