package com.tle.core.activation.workflow;

import java.util.Date;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.activation.ActivateRequest;
import com.tle.core.activation.ActivateRequestDao;
import com.tle.core.security.impl.SecureOnCall;

/**
 * @author Aaron
 */
@SecureOnCall(priv = "DEACTIVATE_ACTIVATION_ITEM")
public class DeactivateOperation extends AbstractBulkableActivationOperation
{
	@AssistedInject
	public DeactivateOperation()
	{
		super();
	}

	@AssistedInject
	public DeactivateOperation(@Assisted long id)
	{
		super(id);
	}

	@Override
	protected boolean doOperation(ActivateRequest request, ActivateRequestDao dao)
	{
		Date now = params.getDateNow();

		if( request.getUntil().after(now) && request.getStatus() == ActivateRequest.TYPE_ACTIVE )
		{
			Date cancellationDate = (Date) now.clone();

			// Just before 'from'/'now': Don't want crossover!
			cancellationDate.setTime(cancellationDate.getTime() - 1);

			request.setStatus(ActivateRequest.TYPE_INACTIVE);
			request.setUntil(cancellationDate);
			dao.update(request);
		}
		return true;
	}
}
