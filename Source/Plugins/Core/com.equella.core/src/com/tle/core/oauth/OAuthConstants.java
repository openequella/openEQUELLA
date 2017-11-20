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

package com.tle.core.oauth;

@SuppressWarnings("nls")
public final class OAuthConstants
{
	private OAuthConstants()
	{
		throw new Error();
	}

	public static final String PRIV_CREATE_OAUTH_CLIENT = "CREATE_OAUTH_CLIENT";
	public static final String PRIV_EDIT_OAUTH_CLIENT = "EDIT_OAUTH_CLIENT";
	public static final String PRIV_DELETE_OAUTH_CLIENT = "DELETE_OAUTH_CLIENT";

	public static final String PRIV_ADMINISTER_OAUTH_TOKENS = "ADMINISTER_OAUTH_TOKENS";

	// http://tools.ietf.org/html/draft-ietf-oauth-v2-23
	/**
	 * The request is missing a required parameter, includes an unsupported
	 * parameter value, repeats a parameter, includes multiple credentials,
	 * utilizes more than one mechanism for authenticating the client, or is
	 * otherwise malformed.
	 */
	public static final String ERROR_INVALID_REQUEST = "invalid_request";
	/**
	 * Client authentication failed (e.g. unknown client, no client
	 * authentication included, or unsupported authentication method). The
	 * authorization server MAY return an HTTP 401 (Unauthorized) status code to
	 * indicate which HTTP authentication schemes are supported. If the client
	 * attempted to authenticate via the "Authorization" request header field,
	 * the authorization server MUST respond with an HTTP 401 (Unauthorized)
	 * status code, and include the "WWW-Authenticate" response header field
	 * matching the authentication scheme used by the client.
	 */
	public static final String ERROR_INVALID_CLIENT = "invalid_client";
	/**
	 * The provided authorization grant (e.g. authorization code, resource owner
	 * credentials) is invalid, expired, revoked, does not match the redirection
	 * URI used in the authorization request, or was issued to another client.
	 */
	public static final String ERROR_INVALID_GRANT = "invalid_grant";
	/**
	 * The authorization grant type is not supported by the authorization
	 * server.
	 */
	public static final String ERROR_UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";
	/**
	 * The client is not authorized to request an access token using this
	 * method.
	 */
	public static final String ERROR_UNAUTHORIZED_CLIENT = "unauthorized_client";
	/**
	 * The resource owner or authorization server denied the request.
	 */
	public static final String ERROR_ACCESS_DENIED = "access_denied";
	/**
	 * The authorization server does not support obtaining an access token using
	 * this method.
	 */
	public static final String ERROR_UNSUPPORTED_RESPONSE_TYPE = "unsupported_response_type";
	/**
	 * The requested scope is invalid, unknown, or malformed.
	 */
	public static final String ERROR_INVALID_SCOPE = "invalid_scope";
	/**
	 * The authorization server encountered an unexpected condition which
	 * prevented it from fulfilling the request.
	 */
	public static final String ERROR_SERVER_ERROR = "server_error";
	/**
	 * The authorization server is currently unable to handle the request due to
	 * a temporary overloading or maintenance of the server.
	 */
	public static final String ERROR_TEMPORARILY_UNAVAILABLE = "temporarily_unavailable";
}
