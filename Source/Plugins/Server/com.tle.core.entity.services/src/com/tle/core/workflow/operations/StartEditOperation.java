package com.tle.core.workflow.operations;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.common.security.SecurityConstants;
import com.tle.core.security.impl.SecureOnCall;

@SecureOnCall(priv = SecurityConstants.EDIT_ITEM)
public class StartEditOperation extends AbstractStartEditOperation
{
	@AssistedInject
	protected StartEditOperation(@Assisted boolean modify)
	{
		super(modify, false);
	}
}
