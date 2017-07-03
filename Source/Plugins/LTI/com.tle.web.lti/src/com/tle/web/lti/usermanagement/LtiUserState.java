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

package com.tle.web.lti.usermanagement;

import java.util.Collection;

import com.tle.common.Triple;
import com.tle.common.usermanagement.user.AbstractUserState;
import com.tle.common.usermanagement.user.UserState;
import com.tle.web.lti.LtiData;

public class LtiUserState extends AbstractUserState
{
	private LtiData data;
	private final boolean system;

	public LtiUserState()
	{
		// Empty
		system = false;
	}

	public LtiUserState(UserState userState)
	{
		system = userState.isSystem();
		setAclExpressions(new Triple<Collection<Long>, Collection<Long>, Collection<Long>>(
			userState.getCommonAclExpressions(), userState.getOwnerAclExpressions(),
			userState.getNotOwnerAclExpressions()));
		setAuditable(userState.isAuditable());
		setAuthenticated(userState.isAuthenticated());
		setHostAddress(userState.getHostAddress());
		setHostReferrer(userState.getHostReferrer());
		setInstitution(userState.getInstitution());
		setInternal(userState.isInternal());
		setIpAddress(userState.getIpAddress());
		setLoggedInUser(userState.getUserBean());
		setSessionID(userState.getSessionID());
		setSharePassEmail(userState.getSharePassEmail());
		setToken(userState.getToken());
		setTokenSecretId(userState.getTokenSecretId());
		setWasAutoLoggedIn(userState.wasAutoLoggedIn());
		getUsersGroups().addAll(userState.getUsersGroups());
		getUsersRoles().addAll(userState.getUsersRoles());
	}

	public LtiData getData()
	{
		return data;
	}

	public void setData(LtiData data)
	{
		this.data = data;
	}

	@Override
	public boolean isSystem()
	{
		return system;
	}
}
