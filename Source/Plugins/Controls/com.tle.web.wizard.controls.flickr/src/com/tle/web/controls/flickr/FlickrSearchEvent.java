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

package com.tle.web.controls.flickr;

import java.util.Date;

import com.tle.core.flickr.FlickrSearchParameters;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;

/**
 * @author larry
 */
public class FlickrSearchEvent extends AbstractSearchEvent<FlickrSearchEvent>
{
	private FlickrSearchParameters params;
	private boolean moreThanKeywordFilter;

	protected FlickrSearchEvent(SectionId sectionId, FlickrSearchParameters params)
	{
		super(sectionId);
		this.params = params;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchEventListener<FlickrSearchEvent> listener)
		throws Exception
	{
		listener.prepareSearch(info, this);
	}

	/**
	 * Set the userFiltered boolean to true if either date is non-null.
	 * 
	 * @param dateRange an array of 2 dates, in order of the earlier and the
	 *            later either of which may be null.
	 */
	public void filterByDateRange(Date[] dateRange)
	{
		if( dateRange != null && dateRange.length > 0 )
		{
			userFiltered = dateRange[0] != null || (dateRange.length > 1 && dateRange[1] != null);
			params.setMinTakenDate(dateRange[0]);
			if( dateRange.length > 1 )
			{
				params.setMaxTakenDate(dateRange[1]);
			}
		}
	}

	public void filterByOwner(String ownerId)
	{
		params.setUserId(ownerId);
	}

	/**
	 * @return the params
	 */
	public FlickrSearchParameters getParams()
	{
		return params;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(FlickrSearchParameters params)
	{
		this.params = params;
	}

	public void setSort(int sort)
	{
		params.setSort(sort);
	}

	public boolean isMoreThanKeywordFilter()
	{
		return moreThanKeywordFilter;
	}

	public void setMoreThanKeywordFilter(boolean moreThanKeywordFilter)
	{
		this.moreThanKeywordFilter = moreThanKeywordFilter;
	}
}