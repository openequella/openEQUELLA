package com.tle.web.oauth.filter;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.dytech.edge.exceptions.WebException;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.tle.core.guice.Bind;
import com.tle.core.oauth.OAuthConstants;
import com.tle.core.oauth.OAuthUserState;
import com.tle.core.user.UserState;
import com.tle.web.core.filter.UserStateHook;
import com.tle.web.core.filter.UserStateResult;
import com.tle.web.core.filter.UserStateResult.Result;
import com.tle.web.oauth.OAuthWebConstants;
import com.tle.web.oauth.service.OAuthWebService;

/**
 * Handles OAuth version 2.
 * 
 * @see
 * @author Aaron
 */
@Bind
@Singleton
public class OAuthUserStateHook implements UserStateHook
{
	@Inject
	private OAuthWebService oauthWebService;

	@SuppressWarnings("nls")
	@Override
	public UserStateResult getUserState(HttpServletRequest request, UserState existingUserState) throws WebException
	{
		// X-Authorization header
		final String xauth = request.getHeader(OAuthWebConstants.HEADER_X_AUTHORIZATION);
		if( xauth != null )
		{
			// should be "access_token=hghjghjghjg"
			final String[] token = xauth.split("=");
			if( token[0].equals(OAuthWebConstants.AUTHORIZATION_ACCESS_TOKEN) )
			{
				if( token.length == 2 )
				{
					final String tokenData = token[1];
					return userStateFromToken(request, tokenData, false);
				}
				throw new WebException(403, OAuthConstants.ERROR_ACCESS_DENIED,
					"Invalid access_token format in X-Authorization header");
			}
		}

		// Query string param
		final String tokenParamValue = request.getParameter(OAuthWebConstants.AUTHORIZATION_ACCESS_TOKEN);
		if( !Strings.isNullOrEmpty(tokenParamValue) )
		{
			return userStateFromToken(request, tokenParamValue, true);
		}

		return null;
	}

	private UserStateResult userStateFromToken(HttpServletRequest request, String tokenData, boolean session)
	{
		final OAuthUserState userState = oauthWebService.getUserState(tokenData, request);
		userState.setAuditable(session);
		return new UserStateResult(userState, session ? Result.LOGIN_SESSION : Result.LOGIN_NO_SESSION_CREATION);
	}

	@Override
	public boolean isInstitutional()
	{
		return true;
	}
}
