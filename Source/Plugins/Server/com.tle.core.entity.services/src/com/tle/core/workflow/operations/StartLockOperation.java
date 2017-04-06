package com.tle.core.workflow.operations;

import javax.inject.Inject;

import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.ItemLock;
import com.tle.core.services.LockingService;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;

public class StartLockOperation extends AbstractWorkflowOperation
{
	@Inject
	private LockingService lockingService;
	private boolean dontRelock;

	protected StartLockOperation(boolean dontRelock)
	{
		this.dontRelock = dontRelock;
	}

	@AssistedInject
	protected StartLockOperation()
	{
		this(false);
	}

	@Override
	public boolean execute()
	{
		// Attempt to lock the item
		if( dontRelock )
		{
			final ItemLock lock = lockingService.getLock(getItem());
			if( lock != null )
			{
				final UserState userState = CurrentUser.getUserState();
				if( lock.getUserID().equals(userState.getUserBean().getUniqueID()) )
				{
					//Don't care, it's our lock.
					return false;
				}
			}
		}

		final ItemLock locedAfterDark = lockingService.lockItem(getItem());
		params.getSecurityStatus().setLock(locedAfterDark);
		return false;
	}
}
