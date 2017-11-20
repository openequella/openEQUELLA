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

package com.tle.core.mimetypes.dao;

import java.util.Collection;
import java.util.List;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;
import com.tle.core.mimetypes.MimeTypesSearchResults;

public interface MimeEntryDao extends GenericInstitutionalDao<MimeEntry, Long>
{
	MimeTypesSearchResults searchByMimeType(String mimeType, int offset, int length);

	MimeTypesSearchResults searchAll(String query, int offset, int length);

	List<MimeEntry> getEntriesForExtensions(Collection<String> extensions);
}
