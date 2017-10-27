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

package com.tle.core.usermanagement.standard.wrapper;

import java.util.Collections;
import java.util.Set;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.beans.usermanagement.standard.wrapper.SuspendedUserWrapperSettings;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.exceptions.DisabledException;
import com.tle.plugins.ump.AbstractUserDirectory;

@Bind
public class SuspendedUserWrapper extends AbstractUserDirectory
{
	private Set<String> suspendedUsers;

	@Override
	protected boolean initialise(UserManagementSettings settings)
	{
		this.suspendedUsers = ((SuspendedUserWrapperSettings) settings).getSuspendedUsers();
		if( suspendedUsers == null )
		{
			suspendedUsers = Collections.emptySet();
		}
		return false;
	}

	@Override
	public void initUserState(ModifiableUserState state)
	{
		if( suspendedUsers.contains(state.getUserBean().getUniqueID()) )
		{
			throw new DisabledException("User account has been suspended");
		}
	}
}
