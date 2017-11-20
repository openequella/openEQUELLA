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

package com.tle.web.search.sort;

import javax.inject.Inject;

import com.tle.common.settings.standard.SearchSettings;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;

public class SortOptionsSection extends StandardSortOptionsSection<FreetextSearchEvent>
{
	@Inject
	private ConfigurationService configService;

	@Override
	protected String getDefaultSearch(SectionInfo info)
	{
		if( !info.getBooleanAttribute(SectionInfo.KEY_FOR_URLS_ONLY) )
		{
			String defaultSort = configService.getProperties(new SearchSettings()).getDefaultSearchSort();
			if( defaultSort != null )
			{
				return defaultSort.toLowerCase();
			}
		}
		return null;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		// So that it gets cached in the context after the first time
		sortOptions.setAlwaysSelectForBookmark(true);
	}
}
