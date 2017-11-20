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

package com.tle.mycontent;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public final class MyContentConstants
{
	public static final String MY_CONTENT_UUID = "6b356e2e-e6a0-235a-5730-15ad1d8ad630";
	public static final String MY_CONTENT_SCHEMA_UUID = "2df3df71-bbff-da39-e0c7-52a856ef8b49";

	public static final String NAME_NODE = "name";
	public static final String KEYWORDS_NODE = "keywords";
	public static final String CONTENT_TYPE_NODE = "content_type";
	public static final String SELECTABLE_ID = "mycon";

	public static final String MYCONTENT_SELECTION = "selected_mycon";

	private MyContentConstants()
	{
		throw new Error();
	}
}
