package com.tle.core.oauth.dao;

import com.tle.common.oauth.beans.OAuthClient;
import com.tle.core.dao.AbstractEntityDao;

/**
 * @author aholland
 */
public interface OAuthClientDao extends AbstractEntityDao<OAuthClient>
{
	OAuthClient getByClientIdAndRedirectUrl(String clientId, String redirectUrl);

	/**
	 * Do not use except for validation
	 * 
	 * @param clientId
	 * @return
	 */
	OAuthClient getByClientIdOnly(String clientId);

	void changeUserId(String fromUserId, String toUserId);
}
