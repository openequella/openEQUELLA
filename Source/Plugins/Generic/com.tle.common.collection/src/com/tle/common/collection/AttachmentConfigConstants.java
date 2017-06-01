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

package com.tle.common.collection;

@SuppressWarnings("nls")
public final class AttachmentConfigConstants
{
	public static final String SHOW_FULLSCREEN_LINK_KEY = "SHOW_FULLSCREEN_LINK_KEY";
	public static final String SHOW_FULLSCREEN_LINK_NEW_WINDOW_KEY = "SHOW_FULLSCREEN_LINK_NEW_WINDOW_KEY";
	public static final String DISPLAY_MODE_KEY = "displaymode";
	public static final String DISPLAY_MODE_STRUCTURED = "structured";
	public static final String DISPLAY_MODE_THUMBNAIL = "thumbnail";
	public static final String RESTRICT_ATTACHMENTS = "RESTRICT_ATTACHMENTS";
	public static final String VIEW_RESTRICTED_ATTACHMENTS = "VIEW_RESTRICTED_ATTACHMENTS";
	public static final String METADATA_TARGET = "METADATA_TARGET";
	public static final String VIEW_ATTACHMENTS = "VIEW_ATTACHMENTS";

	private AttachmentConfigConstants()
	{
		throw new Error();
	}
}
