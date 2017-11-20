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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;

import com.tle.annotation.Nullable;

public class BrightspaceAppContext
{
	private static final String APP_ID_PARAMETER = "x_a";
	private static final String APP_KEY_PARAMETER = "x_b";
	private static final String CALLBACK_URL_PARAMETER = "x_target";
	private static final String STATE_PARAMETER = "x_state";
	//private static final String TYPE_PARAMETER = "type";
	//private static final String TYPE_PARAMETER_VALUE = "mobile";
	private static final String USER_ID_CALLBACK_PARAMETER = "x_a";
	private static final String USER_KEY_CALLBACK_PARAMETER = "x_b";

	private final String _appId;
	private final String _appKey;
	private String _url;

	/**
	 * Constructs a D2LAppContext with the provided application values
	 *
	 * @param appId The application ID provided by the key tool
	 * @param appKey The application key provided by the key tool
	 * @param url The url of the D2L instance
	 */
	public BrightspaceAppContext(String appId, String appKey, String url)
	{
		_appId = appId;
		_appKey = appKey;
		if( url != null && url.endsWith("/") )
		{
			_url = url.substring(0, url.lastIndexOf("/"));
		}
		else
		{
			_url = url;
		}
	}

	public URI createWebUrlForAuthentication(URI redirectUrl, @Nullable String state)
	{
		try
		{
			URI uri = new URI(_url + BrightspaceConnectorConstants.AUTHENTICATION_SERVICE_URI_PATH + "?"
				+ buildAuthenticationUriQueryString(redirectUrl, state));
			return uri;
		}
		catch( URISyntaxException e )
		{
			return null;
		}
	}

	private HashMap<String, String> getParameters(String queryString)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		String[] strings = queryString.split(("&"));
		for( String s : strings )
		{
			String[] split = s.split("=");
			if( split.length == 2 )
			{
				result.put(split[0], split[1]);
			}
		}
		return result;
	}

	public BrightspaceUserContext createUserContext(URI uri)
	{
		if( uri.getQuery() != null )
		{
			HashMap<String, String> r = getParameters(uri.getQuery());
			//use a mock servlet request so we can retrieve parameters from the uri conveniently
			String userId = r.get(USER_ID_CALLBACK_PARAMETER);
			String userKey = r.get(USER_KEY_CALLBACK_PARAMETER);
			if( userId == null || userKey == null )
			{
				return null;
			}
			return new BrightspaceUserContext(_url, _appId, _appKey, userId, userKey);
		}
		else
		{
			return null;
		}
	}

	public BrightspaceUserContext createUserContext(String userId, String userKey)
	{
		return new BrightspaceUserContext(_url, _appId, _appKey, userId, userKey);
	}

	public BrightspaceUserContext createAnonymousUserContext()
	{
		return new BrightspaceUserContext(_url, _appId, _appKey, null, null);
	}

	/**
	 * Constructs a URI to call for authentication given the target URI provided
	 *
	 * @param callbackUri The target which the D2L server should return to after
	 * authenticating
	 * @return The URI for the user to authenticate against
	 */
	private String buildAuthenticationUriQueryString(URI callbackUri, @Nullable String state)
	{
		String callbackUriString = callbackUri.toString();
		String uriHash = D2LSigner.getBase64HashString(_appKey, callbackUriString);
		String result = APP_ID_PARAMETER + "=" + _appId;
		result += "&" + APP_KEY_PARAMETER + "=" + uriHash;
		try
		{
			result += "&" + CALLBACK_URL_PARAMETER + "=" + URLEncoder.encode(callbackUriString, "UTF-8");
		}
		catch( UnsupportedEncodingException e )
		{
			result += "&" + CALLBACK_URL_PARAMETER + "=" + URLEncoder.encode(callbackUriString);
		}
		if( state != null )
		{
			result += "&" + STATE_PARAMETER + "=" + URLEncoder.encode(state);
		}
		return result;
	}

	public String getAppId()
	{
		return _appId;
	}

	public String getAppKey()
	{
		return _appKey;
	}

	public String getUrl()
	{
		return _url;
	}
}
