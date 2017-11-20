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

package com.tle.web.searching.section;

import java.util.Map;

import com.tle.web.search.actions.StandardShareSearchQuerySection;
import com.tle.web.sections.BookmarkModifier;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.generic.InfoBookmark;

@SuppressWarnings("nls")
public class IntegrationShareSearchQuerySection extends StandardShareSearchQuerySection
{
	private static final String SEARCHURL;

	static
	{
		SEARCHURL = RootSearchSection.SEARCHURL.startsWith("/") ? RootSearchSection.SEARCHURL.substring(1)
			: RootSearchSection.SEARCHURL;
	}

	@Override
	public void setupUrl(InfoBookmark bookmark, RenderContext context)
	{
		url.setValue(context, new BookmarkAndModify(bookmark, new BookmarkModifier()
		{
			@Override
			public void addToBookmark(SectionInfo info, Map<String, String[]> bookmarkState)
			{
				bookmarkState.put(SectionInfo.KEY_PATH, new String[]{institutionService.institutionalise(SEARCHURL)});
			}
		}).getHref());
		url.getState(context).setEditable(false);
	}
}
