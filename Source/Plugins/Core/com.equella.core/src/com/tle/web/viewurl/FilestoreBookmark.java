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

package com.tle.web.viewurl;

import com.tle.beans.item.ItemKey;
import com.tle.common.URLUtils;
import com.tle.core.institution.InstitutionService;
import com.tle.web.sections.Bookmark;

public class FilestoreBookmark implements Bookmark
{
	private final String middle;
	private final String path;
	private final InstitutionService institutionService;
	private final String stagingUuid;

	public FilestoreBookmark(InstitutionService institutionService, ItemKey itemId, String path)
	{
		this.middle = itemId.toString();
		this.institutionService = institutionService;
		this.path = path;
		this.stagingUuid = null;
	}

	public FilestoreBookmark(InstitutionService institutionService, String stagingId, String path)
	{
		this.middle = URLUtils.urlEncode(stagingId) + "/$"; //$NON-NLS-1$
		this.institutionService = institutionService;
		this.path = path;
		this.stagingUuid = stagingId;
	}

	@Override
	public String getHref()
	{
		return institutionService.institutionalise("file/" + middle + '/' //$NON-NLS-1$
			+ URLUtils.urlEncode(path, false));
	}

	public String getStagingUuid()
	{
		return stagingUuid;
	}
}
