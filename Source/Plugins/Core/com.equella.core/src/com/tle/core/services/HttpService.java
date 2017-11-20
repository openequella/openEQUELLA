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

package com.tle.core.services;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.NameValue;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Response;
import com.tle.core.services.impl.ProxyDetails;

/**
 * @author aholland
 */
@NonNullByDefault
public interface HttpService
{
	boolean canAccessInternet();

	Response getWebContent(Request request, @Nullable ProxyDetails proxy);

	Response getWebContent(Request request, @Nullable ProxyDetails proxy, boolean followRedirects);

	/**
	 * The idea is that you can build up the URL with the request object and
	 * "toString" it via this.
	 * 
	 * @param request
	 * @return appendQueryString(request.getUrl(),
	 *         queryString(request.getParams()));
	 */
	String toUrl(Request request);

	/**
	 * Appends a query string to a URL, detecting if the URL already contains a
	 * query string
	 */
	String appendQueryString(String url, @Nullable String queryString);

	/**
	 * @param paramNameValues An even number of strings indicating the parameter
	 *            names and values i.e. "param1", "value1", "param2", "value2"
	 */
	String queryString(String... paramNameValues);

	/**
	 * Turns a list of NameValue into a query string. e.g
	 * param1=val1&param2=val2
	 */
	@Nullable
	String queryString(@Nullable List<NameValue> params);

	/**
	 * URL <em>path</em> encodes a value
	 */
	String urlPathEncode(String value);

	/**
	 * Encodes a URL parameter value
	 */
	String urlParamEncode(String value);

	/**
	 * Checks the code of the response for error codes (404, 500 etc)
	 * 
	 * @param response
	 * @return
	 */
	boolean isError(Response response);
}
