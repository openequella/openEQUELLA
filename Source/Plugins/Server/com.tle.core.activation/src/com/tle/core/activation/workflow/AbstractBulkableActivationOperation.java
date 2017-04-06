package com.tle.core.activation.workflow;

import javax.inject.Inject;

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.ItemActivationId;
import com.tle.core.activation.ActivateRequestDao;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

public abstract class AbstractBulkableActivationOperation extends AbstractWorkflowOperation
{
	private final long activationId;

	@Inject
	private ActivateRequestDao dao;

	public AbstractBulkableActivationOperation(long activationId)
	{
		this.activationId = activationId;
	}

	public AbstractBulkableActivationOperation()
	{
		this(0);
	}

	@Override
	public boolean execute()
	{
		long actId = getActivationId();
		ActivateRequest request = dao.findById(actId);
		return doOperation(request, dao);
	}

	protected abstract boolean doOperation(ActivateRequest request, ActivateRequestDao dao);

	public long getActivationId()
	{
		if( activationId != 0 )
		{
			return activationId;
		}
		else
		{
			return Long.valueOf(params.getAttributes().get(ItemActivationId.PARAM_KEY));
		}
	}
}
