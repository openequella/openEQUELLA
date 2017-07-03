package com.tle.core.item.standard.operations;

import javax.inject.Inject;

import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.ItemLock;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.item.service.ItemLockingService;

public class StartLockOperation extends AbstractStandardWorkflowOperation
{
	@Inject
	private ItemLockingService lockingService;

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
			final ItemLock lock = lockingService.get(getItem());
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

		final ItemLock locedAfterDark = lockingService.lock(getItem());
		params.getSecurityStatus().setLock(locedAfterDark);
		return false;
	}
}
