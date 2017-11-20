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

package com.tle.web.portal.section.admin;

import com.tle.core.portal.service.PortletSearch;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;

/**
 * @author aholland
 */
public class PortletSearchEvent extends AbstractSearchEvent<PortletSearchEvent>
{
	private final PortletSearch search;
	private final PortletSearch unfiltered;

	public PortletSearchEvent(SectionId sectionId, PortletSearch search, PortletSearch unfiltered)
	{
		super(sectionId);
		this.search = search;
		this.unfiltered = unfiltered;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchEventListener<PortletSearchEvent> listener)
		throws Exception
	{
		listener.prepareSearch(info, this);
	}

	public PortletSearch getSearch()
	{
		return search;
	}

	public PortletSearch getUnfilteredSearch()
	{
		return unfiltered;
	}

	public void filterByOwner(String uuid)
	{
		search.setOwner(uuid);
	}

	public void filterByType(String type)
	{
		search.setType(type);
	}

	public void filterByOnlyInstWide(Boolean checked)
	{
		search.setOnlyInstWide(checked);
	}
}
