/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.workflow.operations.tasks;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.common.security.SecurityConstants;
import com.tle.core.security.impl.SecureOnCall;

/**
 * @author jmaginnis
 */
@SecureOnCall(priv = SecurityConstants.EDIT_ITEM)
public class SecureSubmitOperation extends SubmitOperation
{
	@AssistedInject
	public SecureSubmitOperation(@Nullable @Assisted String message)
	{
		super(message);
	}

	@AssistedInject
	public SecureSubmitOperation()
	{
		// no message;
	}
}
