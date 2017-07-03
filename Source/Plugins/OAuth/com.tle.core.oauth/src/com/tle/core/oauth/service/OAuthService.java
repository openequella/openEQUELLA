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

package com.tle.core.oauth.service;

import java.util.List;

import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.oauth.beans.OAuthToken;
import com.tle.core.entity.service.AbstractEntityService;

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
}
