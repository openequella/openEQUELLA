package com.tle.core.activation.workflow;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.activation.ActivateRequest;
import com.tle.core.activation.ActivateRequestDao;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;

public class ReassignActivationOperation extends AbstractStandardWorkflowOperation
{
	@Inject
	private ActivateRequestDao dao;

	private final String fromUserId;
	private final String toUserId;

	@AssistedInject
	public ReassignActivationOperation(@Assisted("fromUserId") String fromUserId, @Assisted("toUserId") String toUserId)
	{
		this.fromUserId = fromUserId;
		this.toUserId = toUserId;
	}

	@Override
	public boolean execute()
	{
		boolean changed = false;
		for( ActivateRequest ar : dao.getAllRequests(params.getItemPack().getItem()) )
		{
			if( ar.getUser().equals(fromUserId) )
			{
				ar.setUser(toUserId);
				dao.update(ar);
				changed = true;
			}
		}
		return changed;
	}
}
