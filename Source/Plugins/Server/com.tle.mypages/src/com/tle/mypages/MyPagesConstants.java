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

package com.tle.mypages;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public final class MyPagesConstants
{
	public static final String PLUGIN_ID = "com.tle.mypages";
	public static final String VIEW_PAGES = "viewpages.jsp";

	public static final String MYPAGES_CONTENT_TYPE = "mypages";

	public static final String SECTION_CONTRIBUTE = "mpc";
	public static final String SECTION_EDITOR = "mpe";
	public static final String SECTION_PAGES = "mpp";
	public static final String SECTION_PAGE_ACTIONS = "mpa";
	public static final String SECTION_TABS = "mpt";
	public static final String SECTION_ROOT = "";

	public static final String URL_MYPAGESEDIT = "/access/mypagesedit.do";

	public static final String MYPAGES_DIRECTORY = "_mypages";

	private MyPagesConstants()
	{
		throw new Error();
	}
}
