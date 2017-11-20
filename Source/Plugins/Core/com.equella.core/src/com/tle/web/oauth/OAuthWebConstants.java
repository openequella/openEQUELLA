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

package com.tle.web.oauth;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import net.oauth.OAuth;

import com.google.common.collect.ImmutableSet;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public final class OAuthWebConstants
{
	// OAuth 1 parameters
	public static final List<String> SINGLE_PARAMETERS = Arrays.asList(OAuth.OAUTH_CONSUMER_KEY, OAuth.OAUTH_TOKEN,
		OAuth.OAUTH_TOKEN_SECRET, OAuth.OAUTH_CALLBACK, OAuth.OAUTH_SIGNATURE_METHOD, OAuth.OAUTH_SIGNATURE,
		OAuth.OAUTH_TIMESTAMP, OAuth.OAUTH_NONCE, OAuth.OAUTH_VERSION);

	public static final String PARAM_CONSUMER_KEY = "oauth_consumer_key";
	public static final String PARAM_SIGNATURE_METHOD = "oauth_signature_method";
	public static final String PARAM_TIMESTAMP = "oauth_timestamp";
	public static final String PARAM_NONCE = "oauth_nonce";
	public static final String PARAM_VERSION = "oauth_version";
	public static final String PARAM_SIGNATURE = "oauth_signature";
	public static final String PARAM_CALLBACK = "oauth_callback";

	public static final String OAUTH_DEFAULT_REDIRECT_URL = "oauth/redirect";
	public static final String OAUTH_DEFAULT_REDIRECT_URL_NAME = "default";
	public static final String OAUTH_TOKEN_URL = "oauth/access_token";

	// public static final String HEADER_AUTHORIZATION = "Authorization";
	public static final String HEADER_X_AUTHORIZATION = "X-Authorization";
	public static final String AUTHORIZATION_ACCESS_TOKEN = "access_token";
	// public static final String AUTHORIZATION_BEARER = "Bearer";

	public static final String PARAM_CODE = "code";
	public static final String PARAM_CLIENT_ID = "client_id";
	public static final String PARAM_CLIENT_SECRET = "client_secret";
	public static final String PARAM_REDIRECT_URI = "redirect_uri";
	public static final String PARAM_STATE = "state";
	public static final String PARAM_RESPONSE_TYPE = "response_type";
	public static final String PARAM_GRANT_TYPE = "grant_type";
	public static final String PARAM_DISPLAY = "display";

	// Returned via client-side flow (4.2)
	// /redirect#access_token=cccdsdsdsd&token_type=equella_api
	public static final String PARAM_ACCESS_TOKEN = "access_token";
	public static final String PARAM_TOKEN_TYPE = "token_type";

	public static final String PARAM_ERROR_DESCRIPTION = "error_description";
	public static final String PARAM_ERROR = "error";

	public static final String DISPLAY_PAGE = "page";
	public static final String DISPLAY_MOBILE = "mobile";

	// Response token type
	// public static final String TOKEN_TYPE_BEARER = "bearer";
	public static final String TOKEN_TYPE_EQUELLA_API = "equella_api";

	// passed to authorize URL
	public static final String RESPONSE_TYPE_CODE = "code";
	public static final String RESPONSE_TYPE_TOKEN = "token";

	// passed to token URL
	public static final String GRANT_TYPE_CODE = "authorization_code";
	public static final String GRANT_TYPE_PASSWORD = "password";
	public static final String GRANT_TYPE_CREDENTIALS = "client_credentials";

	public static final Set<String> RESPONSE_TYPES_ALL = ImmutableSet.of(RESPONSE_TYPE_CODE, RESPONSE_TYPE_TOKEN);

	// Noli me tangere constructor, because Sonar likes it that way for
	// non-instantiated utility classes
	private OAuthWebConstants()
	{
		throw new Error();
	}
}
