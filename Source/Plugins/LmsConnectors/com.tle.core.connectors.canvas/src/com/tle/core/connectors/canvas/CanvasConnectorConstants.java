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

package com.tle.core.connectors.canvas;

@SuppressWarnings("nls")
public class CanvasConnectorConstants
{
	public static final String CONNECTOR_TYPE = "canvas";
	public static final String FIELD_ACCESS_TOKEN = "canvasToken";
	public static final String FIELD_TOKEN_OK = "tokenOk";

	// ModuleItem post request parameters
	public static final String MODULE_ITEM_TITLE = "module_item[title]";
	public static final String MODULE_ITEM_TYPE = "module_item[type]";
	public static final String MODULE_ITEM_EXTERNAL_TOOL = "ExternalTool";
	public static final String MODULE_ITEM_CONTENT_ID = "module_item[content_id]";
	public static final String MODULE_ITEM_EXTERNAL_URL = "module_item[external_url]";
	public static final String MODULE_ITEM_NEW_TAB = "module_item[new_tab]";

	public static final String COURSE_STATE_UNPUBLISHED = "unpublished";
	public static final String COURSE_STATE_AVAILABLE = "available";
}
