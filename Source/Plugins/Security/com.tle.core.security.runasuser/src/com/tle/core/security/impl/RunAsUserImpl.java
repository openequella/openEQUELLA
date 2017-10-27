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

package com.tle.core.security.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.WebAuthenticationDetails;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.guice.Bind;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.security.RunAsUser;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.user.UserService;
import com.tle.core.services.user.impl.RunAsUserState;
import com.tle.exceptions.UserException;

/**
 * @author Nicholas Read
 */
@Singleton
@Bind(RunAsUser.class)
public class RunAsUserImpl implements RunAsUser
{
	@Inject
	private RunAsInstitution runAs;
	@Inject
	private UserService userService;
	@Inject
	private TLEAclManager aclManager;

	@Override
	public <V> V execute(Institution institution, String userID, Callable<V> callable)
	{
		return runAs.execute(getNewUserState(institution, userID), callable);
	}

	@Override
	public void execute(Institution institution, String userID, Runnable runnable)
	{
		runAs.execute(getNewUserState(institution, userID), Executors.callable(runnable));
	}

	@Override
	public void execute(Institution institution, UserState userState, Runnable runnable)
	{
		UserState newUserState = new RunAsUserState(userState.getUserBean(), institution, aclManager, userService,
			userState.isSystem());
		runAs.execute(newUserState, Executors.callable(runnable));
	}

	@SuppressWarnings("nls")
	private UserState getNewUserState(Institution institution, String userID)
	{
		Institution originalInstitution = CurrentInstitution.get();
		try
		{
			CurrentInstitution.set(institution);
			UserBean userBean = userService.getInformationForUser(userID);
			if( userBean != null )
			{
				return new RunAsUserState(userBean, institution, aclManager, userService, false);
			}

			UserException u = new UserException("User could not be found: " + userID);
			u.setShowStackTrace(false);
			throw u;
		}
		finally
		{
			CurrentInstitution.set(originalInstitution);
		}
	}

	@Override
	public void executeAsGuest(Institution institution, Runnable runnable, WebAuthenticationDetails details)
	{
		runAs.execute(userService.authenticateAsGuest(details), Executors.callable(runnable));
	}
}
