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

package com.tle.core.connectors.brightspace;

@SuppressWarnings("nls")
public class BrightspaceConnectorConstants
{
	public static final String CONNECTOR_TYPE = "brightspace";
	public static final String AUTH_URL = "brightspaceauth";

	public static final String AUTHENTICATION_SERVICE_URI_PATH = "/d2l/auth/api/token";

	public static final String FIELD_APP_OK = "appOk";
	public static final String FIELD_APP_ID = "appId";
	public static final String FIELD_APP_KEY = "appKey";

	public static final String FIELD_ADMIN_USER_ID = "adminUserId";
	public static final String FIELD_ADMIN_USER_KEY = "adminUserKeyEncrypted";
	public static final String FIELD_ADMIN_OK = "adminOk";
	public static final String FIELD_ADMIN_USERNAME = "adminUsername";

	public static final String SESSION_KEY_USER_CONTEXT = "Brightspace.UserContext";
	public static final String SESSION_KEY_ADMIN_USER_CONTEXT = "Brightspace.AdminUserContext";
	public static final String SESSION_KEY_USER_ID = "Brightspace.UserId";
	public static final String SESSION_KEY_USER_KEY = "Brightspace.UserKey";

	public static final String STATE_KEY_FORWARD_URL = "forwardUrl";
	public static final String STATE_KEY_POSTFIX_KEY = "postfixKey";
}
