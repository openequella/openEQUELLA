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

import javax.inject.Inject;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.beans.usermanagement.standard.user.SharePassUserState;
import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.guice.Bind;
import com.tle.core.usermanagement.standard.service.SharePassService;
import com.tle.plugins.ump.AbstractUserDirectory;

@Bind
public class SharePassWrapper extends AbstractUserDirectory
{
	public static final String TOKEN_PREFIX = "TLESP:"; //$NON-NLS-1$

	@Inject
	private SharePassService sharePassService;

	@Override
	protected boolean initialise(UserManagementSettings settings)
	{
		return false;
	}

	@Override
	public ModifiableUserState authenticateToken(final String token)
	{
		String sharePassId = parseToken(token);
		if( sharePassId != null )
		{
			String email = sharePassService.activatePasses(sharePassId);
			if( email != null )
			{
				return new SharePassUserState(CurrentInstitution.get(), email, token);
			}
		}
		return null;
	}

	private String parseToken(String token)
	{
		if( token.startsWith(TOKEN_PREFIX) )
		{
			return token.substring(TOKEN_PREFIX.length());
		}
		else
		{
			return null;
		}
	}

	@Override
	public VerifyTokenResult verifyUserStateForToken(UserState userState, String token)
	{
		String sharePassId = parseToken(token);
		if( sharePassId != null )
		{
			return Check.bothNullOrDeepEqual(token, userState.getToken()) ? VerifyTokenResult.VALID
				: VerifyTokenResult.INVALID;
		}
		return VerifyTokenResult.PASS;
	}
}
