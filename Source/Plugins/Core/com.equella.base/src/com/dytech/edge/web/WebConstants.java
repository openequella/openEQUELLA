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

package com.dytech.edge.web;

/**
 * @author jmaginnis
 */
@SuppressWarnings("nls")
public final class WebConstants
{
	public static final String TOKEN_AUTHENTICATION_PARAM = "token";

	public static final String KEY_SESSION = "$SESSION$";
	public static final String KEY_NO_SESSION = "$NO$SESSION";
	public static final String KEY_USERSTATE = "$USER$STATE";
	public static final String KEY_LOCALE = "$LOCALE$";
	public static final String KEY_TIMEZONE = "$TIMEZONE$";
	public static final String KEY_LOGIN_EXCEPTION = "$LOGIN$EXCEPTION$";

	public static final String PAGE_AFTER_LOGON_KEY = "pageAfterLogon";
	public static final String DEFAULT_PAGE_AFTER_LOGIN = "logonnotice.do";

	public static final String ACCESS_PATH = "/access/";
	public static final String SIGNON_PATH = "/signon.do";

	public static final String DASHBOARD_PAGE = "home.do";
	public static final String DASHBOARD_PAGE_PRIVILEGE = "DASHBOARD_PAGE";

	public static final String SEARCHING_PAGE = "searching.do";
	public static final String SEARCH_PAGE_PRIVILEGE = "SEARCH_PAGE";

	public static final String HIERARCHY_PAGE = "hierarchy.do";
	public static final String HIERARCHY_PAGE_PRIVILEGE = "HIERARCHY_PAGE";

	public static final String CLOUDSEARCH_PAGE = "cloudsearch.do";

	// TODO: extension point?
	public static final String DEFAULT_HOME_PAGE = DASHBOARD_PAGE;

	private WebConstants()
	{
		throw new Error();
	}
}
