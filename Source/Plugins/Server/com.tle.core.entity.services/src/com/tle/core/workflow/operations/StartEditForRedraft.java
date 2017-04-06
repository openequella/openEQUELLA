package com.tle.core.workflow.operations;

import com.tle.core.security.impl.SecureOnCall;

@SecureOnCall(priv = "REDRAFT_ITEM")
public class StartEditForRedraft extends AbstractStartEditOperation
{
	protected StartEditForRedraft()
	{
		super(true, false);
	}
}
