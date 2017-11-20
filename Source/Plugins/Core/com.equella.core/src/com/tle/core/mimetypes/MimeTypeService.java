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

package com.tle.core.mimetypes;

import java.util.Collection;
import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.mime.MimeEntry;
import com.tle.core.TextExtracterExtension;

@NonNullByDefault
public interface MimeTypeService
{
	@Nullable
	MimeEntry getEntryForFilename(String filename);

	/**
	 * @param filename
	 * @return If the extension is not known then application/octet-stream is
	 *         returned
	 */
	String getMimeTypeForFilename(String filename);

	@Nullable
	MimeEntry getEntryForMimeType(String mimeType);

	MimeEntry getEntryForId(long id);

	/**
	 * Implied wildcard at end of mimeType
	 * 
	 * @param mimeType
	 * @return
	 */
	MimeTypesSearchResults searchByMimeType(String mimeType, int offset, int length);

	/**
	 * Implied wildcard at end of mimeType
	 * 
	 * @param filename File name or extension
	 * @return
	 */
	Collection<MimeEntry> searchByFilename(String filename);

	void saveOrUpdate(long mimeEntryId, MimeEntryChanges changes);

	void delete(MimeEntry mimeEntry);

	void delete(long id);

	<T> List<T> getListFromAttribute(MimeEntry entry, String key, Class<T> entryType);

	@Nullable
	<T> T getBeanFromAttribute(MimeEntry entry, String key, Class<T> entryType);

	void setListAttribute(MimeEntry entry, String key, @Nullable Collection<?> list);

	<T> void setBeanAttribute(MimeEntry entry, String key, @Nullable T bean);

	void clearAllForPrefix(MimeEntry entry, String keyPrefix);

	List<TextExtracterExtension> getAllTextExtracters();

	List<TextExtracterExtension> getTextExtractersForMimeEntry(MimeEntry mimeEntry);

	@Nullable
	String getMimeEntryForAttachment(Attachment attachment);

	interface MimeEntryChanges
	{
		void editMimeEntry(MimeEntry entry);
	}
}
