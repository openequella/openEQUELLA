package com.tle.web.oauth;

import java.util.Collection;

import com.tle.common.Triple;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.oauth.beans.OAuthToken;
import com.tle.core.oauth.OAuthUserState;
import com.tle.core.oauth.dao.OAuthClientDao;
import com.tle.core.user.AbstractUserState;
import com.tle.core.user.UserState;

/**
 * @author Aaron
 */
public class OAuthUserStateImpl extends AbstractUserState implements OAuthUserState
{
	private static final long serialVersionUID = 8603815613441342333L;

	private final String oauthToken;
	private final String clientUuid;
	// Serialised instances will refresh client via the (serialised) client Uuid
	private transient OAuthClient client; // NOSONAR
	private final boolean system;

	public OAuthUserStateImpl(UserState userState, OAuthToken token)
	{
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
		this.client = token.getClient();
		this.oauthToken = token.getToken();
		this.clientUuid = client.getUuid();
		this.system = userState.isSystem();
	}

	@Override
	public boolean isSystem()
	{
		return system;
	}

	@Override
	public String getOAuthToken()
	{
		return oauthToken;
	}

	@Override
	public String getClientUuid()
	{
		return clientUuid;
	}

	@Override
	public OAuthClient getClient(OAuthClientDao clientDao)
	{
		if( client == null )
		{
			client = clientDao.getByUuid(clientUuid);
		}
		return client;
	}
}
