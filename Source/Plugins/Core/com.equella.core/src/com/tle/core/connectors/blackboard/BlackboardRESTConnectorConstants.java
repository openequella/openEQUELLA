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

package com.tle.core.connectors.blackboard;

@SuppressWarnings("nls")
public final class BlackboardRESTConnectorConstants {

  // public static final String AUTHENTICATION_SERVICE_URI_PATH =
  // "/learn/api/public/v1/oauth2/token";
  public static final String AUTHENTICATIONCODE_SERVICE_URI_PATH =
      "/learn/api/public/v1/oauth2/authorizationcode";

  //  public static final String SESSION_KEY_USER_CONTEXT = "BbRest.UserContext";
  //  public static final String SESSION_KEY_ADMIN_USER_CONTEXT = "BbRest.AdminUserContext";
  public static final String SESSION_KEY_USER_ID = "BbRest.UserId";
  //  public static final String SESSION_KEY_USER_KEY = "BbRest.UserKey";
  public static final String SESSION_CODE = "BbRest.Code";
  public static final String SESSION_TOKEN = "BbRest.Token";

  public static final String CONNECTOR_TYPE = "blackboardrest";

  public static final String FIELD_API_KEY = "apiKey";
  public static final String FIELD_API_SECRET = "apiSecret";

  //  public static final String FIELD_APP_ID = "appId";
  //  public static final String FIELD_APP_KEY = "appKey";

  public static final String STATE_KEY_FORWARD_URL = "forwardUrl";
  public static final String STATE_KEY_POSTFIX_KEY = "postfixKey";

  public static final String AUTH_URL = "blackboardrestauth";

  private BlackboardRESTConnectorConstants() {
    throw new Error();
  }
}
