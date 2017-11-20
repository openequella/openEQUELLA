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

package com.tle.web.cloud.viewable;

import com.tle.core.institution.InstitutionService;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ViewItemUrl;

/**
 * @author Aaron
 */
public class CloudViewItemUrl extends ViewItemUrl
{
	public CloudViewItemUrl(SectionInfo info, String itemdir, UrlEncodedString filepath,
		InstitutionService institutionService, int flags)
	{
		super(info, itemdir, filepath, null, institutionService, flags);
	}
}
