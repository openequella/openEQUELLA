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

package com.tle.common.usermanagement.user;

import java.util.Set;

import com.tle.common.usermanagement.user.valuebean.UserBean;

/**
 * @author Nicholas Read
 */
public final class CurrentUser
{
	private static ThreadLocal<UserState> stateLocal = new ThreadLocal<UserState>();

	private CurrentUser()
	{
		throw new Error();
	}

	public static UserState getUserState()
	{
		return stateLocal.get();
	}

	public static void setUserState(UserState state)
	{
		stateLocal.set(state);
	}

	public static String getSessionID()
	{
		if( getUserState() == null )
		{
			return null;
		}
		return getUserState().getSessionID();
	}

	public static Set<String> getUsersGroups()
	{
		return getUserState().getUsersGroups();
	}

	public static UserBean getDetails()
	{
		return getUserState().getUserBean();
	}

	public static String getUserID()
	{
		return getUserState().getUserBean().getUniqueID();
	}

	public static String getUsername()
	{
		return getUserState().getUserBean().getUsername();
	}

	public static boolean isGuest()
	{
		return getUserState().isGuest();
	}

	public static boolean wasAutoLoggedIn()
	{
		return getUserState().wasAutoLoggedIn();
	}
}
