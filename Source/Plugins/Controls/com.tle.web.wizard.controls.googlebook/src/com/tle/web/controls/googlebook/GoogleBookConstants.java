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

package com.tle.web.controls.googlebook;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public final class GoogleBookConstants
{
	public static final String ATTACHMENT_TYPE = "googlebook";

	public static final String MIME_TYPE = "equella/attachment-googlebook";

	public static final String PROPERTY_ID = "id";
	public static final String PROPERTY_URL = "url";
	public static final String PROPERTY_THUMB_URL = "thumbUrl";
	public static final String PROPERTY_FORMATS = "formats";
	public static final String PROPERTY_PUBLISHED = "published";
	public static final String PROPERTY_DESCRIPTION = "description";

	private GoogleBookConstants()
	{
		throw new Error();
	}
}
