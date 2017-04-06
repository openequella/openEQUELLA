package com.tle.core.oauth.dao;

import java.util.List;

import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.oauth.beans.OAuthToken;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;

/**
 * @author Aaron
 */
public interface OAuthTokenDao extends GenericInstitutionalDao<OAuthToken, Long>
{
	OAuthToken getToken(String userId, OAuthClient client);

	OAuthToken getToken(String tokenData);

	List<OAuthToken> findAllByClient(OAuthClient client);

	void deleteAllForUser(String userId);

	void changeUserId(String fromUserId, String toUserId);
}
