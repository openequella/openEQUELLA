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

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

public class BrightspaceUserContext implements Serializable
{
	private final String instanceUrl;
	private final String appId;
	private final String appKey;
	private final String userId;
	private final String userKey;

	private long _serverSkewMillis;

	private static final String APP_ID_PARAMETER = "x_a";
	private static final String USER_ID_PARAMETER = "x_b";
	private static final String SIGNATURE_BY_APP_KEY_PARAMETER = "x_c";
	private static final String SIGNATURE_BY_USER_KEY_PARAMETER = "x_d";
	private static final String TIMESTAMP_PARAMETER = "x_t";

	/**
	*
	* Returned when no result can be identified or as an unitialized value.
	* Val=0x00;
	*/
	public final int RESULT_UNKNOWN = 0x00;

	/**
	*
	* Returned when result 200 okay. Val=0x01
	*/
	public final int RESULT_OKAY = 0x01;

	/**
	* Returned when the signature or id was invalid, typically this should
	* trigger a reauthentication. Val=0x02
	*/
	public final int RESULT_INVALID_SIG = 0x02;

	/**
	* Returned if the timestamp was outside of the validity window, this
	* indicates clocks are skewed. The handleResult message automatically
	* corrects the clock so on receiving this message callers typically should
	* retry the same operation. Val=0x03
	*/
	public final int RESULT_INVALID_TIMESTAMP = 0x03;

	/**
	* Returned if the requested operation is not allowed, typically user should
	* be prompted that they need to request different permissions from the
	* administrator. Val = 0x04
	*/
	public final int RESULT_NO_PERMISSION = 0x04;

	/**
	 * Creates a User Context with the provided parameters
	 *
	 * @param url the url of the D2Linstance
	 * @param appId the Application Id
	 * @param appKey The Application Key
	 * @param userId the User Id
	 * @param userKey the User Key
	 */
	public BrightspaceUserContext(String url, String appId, String appKey, String userId, String userKey)
	{
		this.instanceUrl = url;
		this.appId = appId;
		this.appKey = appKey;
		this.userId = userId;
		this.userKey = userKey;
	}

	public String getUserId()
	{
		return userId;
	}

	public String getUserKey()
	{
		return userKey;
	}

	public long getServerSkewMillis()
	{
		return _serverSkewMillis;
	}

	public void setServerSkewMillis(long _serverSkewMillis)
	{
		this._serverSkewMillis = _serverSkewMillis;
	}

	public URI createAuthenticatedUri(String path, String httpMethod)
	{
		int split = path.indexOf("?");
		String query = "";
		if( split >= 0 )
		{
			query = path.substring(split + 1);
			path = path.substring(0, split);
		}
		String queryString = getQueryString(path, query, httpMethod);
		try
		{
			URI uri = new URI(instanceUrl + path + queryString);
			return uri;
		}
		catch( URISyntaxException e )
		{
			e.printStackTrace();
			return null;
		}
	}

	public boolean calculateServerSkewFromResponse(String responseBody)
	{
		long serverTimestampSeconds = tryParseTimestamp(responseBody);
		if( serverTimestampSeconds > 0 )
		{
			long clientTimestampMilliseconds = System.currentTimeMillis();
			_serverSkewMillis = serverTimestampSeconds * 1000 - clientTimestampMilliseconds;
			return true;
		}
		return false;
	}

	/**
	 * Constructs the query string with the appropriate parameters for
	 * authentication with the D2L authentication system.
	 *
	 * @param signature The signature based on the path, http method and time
	 * @param timestamp The timestamp for the query string
	 * @return A query string with the relevant authentication parameters
	 */
	private String buildAuthenticatedUriQueryString(String signature, long timestamp)
	{
		String queryString = "?" + APP_ID_PARAMETER + "=" + appId;
		if( getUserId() != null )
		{
			queryString += "&" + USER_ID_PARAMETER + "=" + getUserId();
		}
		queryString += "&" + SIGNATURE_BY_APP_KEY_PARAMETER;
		queryString += "=" + D2LSigner.getBase64HashString(appKey, signature);
		if( getUserId() != null )
		{
			queryString += "&" + SIGNATURE_BY_USER_KEY_PARAMETER;
			queryString += "=" + D2LSigner.getBase64HashString(getUserKey(), signature);
		}
		queryString += "&" + TIMESTAMP_PARAMETER + "=" + timestamp;
		return queryString;
	}

	/**
	 * Provides the unix timestamp adjusted for the approximate delay between
	 * the D2L server and client
	 *
	 * @return The unix timestamp adjusted for the approximate delay between the
	 * D2L server and client
	 */
	private long getAdjustedTimestampInSeconds()
	{
		long timestampMilliseconds = System.currentTimeMillis();
		long adjustedTimestampSeconds = (timestampMilliseconds + _serverSkewMillis) / 1000;
		return adjustedTimestampSeconds;
	}

	/**
	 * Constructs the query string with the appropriate parameters for
	 * authentication with the D2L authentication system.
	 *
	 * @param path The absolute server path of the api (ie. /d2l/api/versions/)
	 * @param httpMethod The http method to access the url with (GET,POST,etc.)
	 * @return The query string with the appropriate parameters for
	 * authentication
	 */
	private String getQueryString(String path, String query, String httpMethod)
	{
		long adjustedTimestampSeconds = getAdjustedTimestampInSeconds();
		String signature = formatSignature(path, httpMethod, adjustedTimestampSeconds);
		String queryString = buildAuthenticatedUriQueryString(signature, adjustedTimestampSeconds);

		if( !Strings.isNullOrEmpty(query) )
		{
			queryString += "&" + query;
		}
		return queryString;
	}

	/**
	 * Creates a signature formatted to the D2L specifications for connecting to
	 * a given path
	 *
	 * @param path The absolute server path of the api (ie. /d2l/api/versions/)
	 * @param httpMethod The http method to access the url with (GET,POST,etc.)
	 * @param timestampSeconds
	 * @return
	 */
	private static String formatSignature(String path, String httpMethod, long timestampSeconds)
	{
		return httpMethod.toUpperCase() + "&" + URI.create(path).getPath().toLowerCase() + "&" + timestampSeconds;
	}

	/**
	 * Returns the timestamp returned by the server if the time between the request and when the server received it is too large
	 * 
	 * @param timestampMessage The message returned by the D2L Server
	 * @return The timestamp returned by the server or 0 if no timestamp returned
	 */
	private long tryParseTimestamp(String timestampMessage)
	{
		long result;
		Pattern p = Pattern.compile("Timestamp out of range\\r*\\n*\\s*(([0-9]+))");

		Matcher m = p.matcher(timestampMessage);
		boolean match = m.matches();

		if( match && m.groupCount() >= 2 )
		{
			result = Long.parseLong(m.group(1));
			return result;
		}
		result = 0;
		return result;
	}

	public int interpretResult(int resultCode, String responseBody)
	{
		if( resultCode == 200 )
		{
			return RESULT_OKAY;
		}
		else if( resultCode == 403 )
		{
			if( calculateServerSkewFromResponse(responseBody) )
			{
				return RESULT_INVALID_TIMESTAMP;
			}
			else if( responseBody.toLowerCase().equals("invalid token")
				|| responseBody.toLowerCase().equals("token expired") )
			{
				return RESULT_INVALID_SIG;
			}
			else
			{
				return RESULT_NO_PERMISSION;
			}
		}
		return RESULT_UNKNOWN;
	}
}
