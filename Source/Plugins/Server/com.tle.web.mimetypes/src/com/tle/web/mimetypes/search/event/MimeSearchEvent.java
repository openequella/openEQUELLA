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

package com.tle.web.mimetypes.search.event;

import com.tle.common.search.DefaultSearch;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;

public class MimeSearchEvent extends AbstractSearchEvent<MimeSearchEvent>
{
	private final DefaultSearch search;

	public MimeSearchEvent()
	{
		// broadcast
		super(null);
		search = new DefaultSearch();
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchEventListener<MimeSearchEvent> listener)
		throws Exception
	{
		listener.prepareSearch(info, this);
	}

	public DefaultSearch getSearch()
	{
		return search;
	}
}
