package com.tle.core.item.standard.operations;

import com.tle.common.security.SecurityConstants;
import com.tle.core.security.impl.SecureOnCall;

/**
 * Same as StartEditOperation, but permission is CREATE_ITEM, not EDIT_ITEM
 * 
 * @author aholland
 */
@SecureOnCall(priv = SecurityConstants.CREATE_ITEM)
public class StartEditForMoveOperation extends AbstractStartEditOperation
{
	protected StartEditForMoveOperation()
	{
		super(true, false);
	}
}
