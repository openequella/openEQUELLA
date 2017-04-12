package com.tle.core.oauth;

import com.tle.common.oauth.beans.OAuthClient;
import com.tle.core.oauth.dao.OAuthClientDao;
import com.tle.core.user.UserState;

/**
 * @author Aaron
 */
public interface OAuthUserState extends UserState
{
	String getOAuthToken();

	String getClientUuid();

	/**
	 * The dao is required in case the transient value is missing
	 * 
	 * @return
	 */
	OAuthClient getClient(OAuthClientDao dao);
}
