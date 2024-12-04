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

package com.tle.core.oauth.dao;

import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.oauth.beans.OAuthToken;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;
import java.util.List;

public interface OAuthTokenDao extends GenericInstitutionalDao<OAuthToken, Long> {
  OAuthToken getToken(String userId, OAuthClient client);

  OAuthToken getToken(String tokenData);

  List<OAuthToken> findAllByClient(OAuthClient client);

  void deleteAllForUser(String userId);

  void changeUserId(String fromUserId, String toUserId);

  void deleteByToken(String token);
}
