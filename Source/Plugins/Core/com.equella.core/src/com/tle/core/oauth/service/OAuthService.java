/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.oauth.beans.OAuthToken;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.oauth.event.DeleteOAuthTokensEvent;
import java.util.List;

@SuppressWarnings("nls")
public interface OAuthService extends AbstractEntityService<OAuthClientEditingBean, OAuthClient> {
  String ENTITY_TYPE = "OAUTH_CLIENT";

  OAuthClient getByClientIdAndRedirectUrl(String clientId, String redirectUrl);

  OAuthToken getOrCreateToken(String userId, String username, OAuthClient client, String code);

  /**
   * Retrieve the OAuthToken with a {@code token} field matching {@code tokenData}, or {@code null}
   * if no matching token is found. If a matched token is found which has been expired, then it will
   * be removed from the database and {@code null} will be returned.
   */
  OAuthToken getToken(String tokenData);

  /** Has the 'expiry' of this token passed? */
  boolean isExpired(OAuthToken token);

  boolean canAdministerTokens();

  boolean deleteToken(long id);

  /**
   * Delete a token by its value. An {@link DeleteOAuthTokensEvent} will be published as well.
   *
   * @param token Token to be Deleted
   */
  void deleteToken(String token);

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
