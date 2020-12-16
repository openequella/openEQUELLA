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

package com.tle.core.connectors.blackboard.service;

import com.tle.annotation.Nullable;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.blackboard.beans.Token;
import com.tle.core.connectors.service.ConnectorRepositoryImplementation;

public interface BlackboardRESTConnectorService extends ConnectorRepositoryImplementation {
  /**
   * Admin setup function
   *
   * @param appId
   * @param appKey
   * @param brightspaceServerUrl
   * @param forwardUrl
   * @param postfixKey
   * @return
   */
  String getAuthorisationUrl(
      String appId,
      String appKey,
      String brightspaceServerUrl,
      String forwardUrl,
      @Nullable String postfixKey);

  /**
   * The connector object will need to store an encrypted admin token in the DB. Use this method to
   * encrypt the one returned from Blackboard.
   *
   * @param data content to encrypt
   * @return encrypted content
   */
  String encrypt(String data);

  String decrypt(String encryptedData);

  /**
   * Creates a Base 64 encoded string of the connector's key and secret.
   *
   * @param connector used to launch this REST flow
   * @return encoded string of connector key and secret
   */
  String buildBasicAuthorizationCredentials(Connector connector);

  void setAuth(Connector connector, Token token);

  void removeCachedCoursesForConnector(Connector connector);
}
