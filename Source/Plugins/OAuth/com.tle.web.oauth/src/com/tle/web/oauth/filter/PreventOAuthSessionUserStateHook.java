package com.tle.web.oauth.filter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.dytech.edge.exceptions.WebException;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.core.user.UserState;
import com.tle.web.core.filter.UserStateHook;
import com.tle.web.core.filter.UserStateResult;
import com.tle.web.core.filter.UserStateResult.Result;

/**
 * Runs at the very back of the chain, when nothing else has decided that there is a logged in user.
 * If the requested URL is an oauth endpoint, do NOT create a guest session when there is no existing user state.
 * 
 * @author Aaron
 *
 */
@Bind
@Singleton
public class PreventOAuthSessionUserStateHook implements UserStateHook
{
	@Inject
	private UrlService urlService;

	@Override
	public UserStateResult getUserState(HttpServletRequest request, UserState userState) throws WebException
	{
		if( userState == null && isOAuthEndpoint(request) )
		{
			return new UserStateResult(Result.GUEST_NO_SESSION_CREATION);
		}
		return null;
	}

	@Override
	public boolean isInstitutional()
	{
		return true;
	}

	private boolean isOAuthEndpoint(HttpServletRequest request)
	{
		String path = urlService.removeInstitution(request.getRequestURL().toString());
		if( path.startsWith("oauth/") )
		{
			return true;
		}
		return false;
	}
}
