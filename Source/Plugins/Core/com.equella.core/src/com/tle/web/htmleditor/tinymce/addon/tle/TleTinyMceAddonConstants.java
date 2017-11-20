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

package com.tle.web.htmleditor.tinymce.addon.tle;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public final class TleTinyMceAddonConstants
{
	public static final String RESOURCE_LINKER_ID = "tle_reslinker";
	public static final String SCRAPBOOK_PICKER_ID = "tle_scrapbookpicker";
	public static final String FILE_UPLOADER_ID = "tle_fileuploader";

	public static final String FILE_UPLOAD_SELECTABLE = "mcefileupload";

	private TleTinyMceAddonConstants()
	{
		throw new Error();
	}
}
