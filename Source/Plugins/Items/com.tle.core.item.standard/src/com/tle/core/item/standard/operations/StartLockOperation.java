/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
