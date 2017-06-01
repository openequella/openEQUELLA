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

package com.tle.web.mimetypes.service;

import java.net.URL;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.mime.MimeEntry;

@NonNullByDefault
public interface WebMimeTypeService
{
	@Nullable
	MimeEntry getEntryForFilename(String filename);

	String getMimeTypeForFilename(String filename);

	@Nullable
	MimeEntry getEntryForMimeType(String mimeType);

	URL getIconForEntry(@Nullable MimeEntry entry);

	URL getIconForEntry(@Nullable MimeEntry entry, boolean allowCache);

	URL getDefaultIconForEntry(@Nullable MimeEntry entry);

	boolean hasCustomIcon(MimeEntry entry);

	/**
	 * @param entry
	 * @param base64icon May be null, in which case the default icon will be
	 *            restored
	 */
	void setIconBase64(MimeEntry entry, @Nullable String base64Icon);
}
