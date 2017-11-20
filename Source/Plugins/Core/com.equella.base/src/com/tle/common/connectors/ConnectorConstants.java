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

package com.tle.common.connectors;

@SuppressWarnings("nls")
public final class ConnectorConstants
{
	private ConnectorConstants()
	{
		throw new Error();
	}

	public static final String FIELD_TESTED_URL = "testedUrl";
	public static final String SHOW_SUMMARY_KEY = "showSummary";

	public static final String PRIV_CREATE_CONNECTOR = "CREATE_CONNECTOR";
	public static final String PRIV_EDIT_CONNECTOR = "EDIT_CONNECTOR";
	public static final String PRIV_DELETE_CONNECTOR = "DELETE_CONNECTOR";
	public static final String PRIV_VIEWCONTENT_VIA_CONNECTOR = "VIEWCONTENT_VIA_CONNECTOR";
	public static final String PRIV_EXPORT_VIA_CONNECTOR = "EXPORT_VIA_CONNECTOR";
	public static final String PRIV_MANAGE_VIA_CONNECTOR = "MANAGE_VIA_CONNECTOR";
	public static final String PRIV_FIND_USES_ITEM = "FIND_USES_ITEM";
	public static final String PRIV_EXPORT_TO_LMS_ITEM = "EXPORT_TO_LMS_ITEM";
}
