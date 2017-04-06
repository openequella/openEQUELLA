package com.tle.core.oauth.service;

import java.util.List;

import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.oauth.beans.OAuthToken;
import com.tle.core.services.entity.AbstractEntityService;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public interface OAuthService extends AbstractEntityService<OAuthClientEditingBean, OAuthClient>
{
	String ENTITY_TYPE = "OAUTH_CLIENT";

	OAuthClient getByClientIdAndRedirectUrl(String clientId, String redirectUrl);

	OAuthToken getOrCreateToken(String userId, String username, OAuthClient client, String code);

	OAuthToken getToken(String tokenData);

	boolean canAdministerTokens();

	boolean deleteToken(long id);

	void deleteTokens(OAuthClient client);

	List<OAuthToken> listAllTokens();

	/**
	 * Do not use except for validation
	 * 
	 * @param clientId
	 * @return
	 */
	OAuthClient getByClientIdOnly(String clientId);

	OAuthClient saveWithEditingBean(OAuthClientEditingBean clientEditingBean, String lockId, boolean keepLocked);
}
