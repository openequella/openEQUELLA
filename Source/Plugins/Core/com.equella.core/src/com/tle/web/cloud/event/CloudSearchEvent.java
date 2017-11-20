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

package com.tle.web.cloud.event;

import java.util.List;

import com.tle.common.searching.SortField;
import com.tle.core.cloud.search.CloudSearch;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;

public class CloudSearchEvent extends AbstractSearchEvent<CloudSearchEvent>
{
	private CloudSearch cloudSearch = new CloudSearch();

	public CloudSearchEvent(SectionId sectionId)
	{
		super(sectionId);
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchEventListener<CloudSearchEvent> listener)
		throws Exception
	{
		listener.prepareSearch(info, this);
	}

	@Override
	public void filterByTextQuery(String query, boolean includeUnfiltered)
	{
		cloudSearch.setQuery(query);
	}

	public CloudSearch getCloudSearch()
	{
		return cloudSearch;
	}

	public void filterByLanguage(String language)
	{
		cloudSearch.setLanguage(language);
	}

	public void filterByLicence(String licence)
	{
		cloudSearch.setLicence(licence);
	}

	public void filterByPublisher(String publisher)
	{
		cloudSearch.setPublisher(publisher);
	}

	public void filterByEducationLevel(String educationLevel)
	{
		cloudSearch.setEducationLevel(educationLevel);
	}

	public void filterByFormats(List<String> formats)
	{
		cloudSearch.addFormats(formats);
	}

	@Override
	public void setSortFields(boolean reversed, SortField... sort)
	{
		cloudSearch.setSortReversed(reversed);
		cloudSearch.setSortFields(sort);
	}

}
