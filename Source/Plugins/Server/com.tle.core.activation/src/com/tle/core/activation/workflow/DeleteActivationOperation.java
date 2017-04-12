package com.tle.core.activation.workflow;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.activation.ActivateRequest;
import com.tle.core.activation.ActivateRequestDao;
import com.tle.core.security.impl.SecureOnCall;

/**
 * @author Aaron
 */
@SecureOnCall(priv = "DELETE_ACTIVATION_ITEM")
public class DeleteActivationOperation extends AbstractBulkableActivationOperation
{
	@AssistedInject
	public DeleteActivationOperation()
	{
		super();
	}

	@AssistedInject
	public DeleteActivationOperation(@Assisted long requestId)
	{
		super(requestId);
	}

	@Override
	protected boolean doOperation(ActivateRequest request, ActivateRequestDao dao)
	{
		dao.delete(request);
		return true;
	}
}
