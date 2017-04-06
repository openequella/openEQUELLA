package com.tle.web.lti.usermanagement;

import java.util.Collection;

import com.tle.common.Triple;
import com.tle.core.user.AbstractUserState;
import com.tle.core.user.UserState;
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
