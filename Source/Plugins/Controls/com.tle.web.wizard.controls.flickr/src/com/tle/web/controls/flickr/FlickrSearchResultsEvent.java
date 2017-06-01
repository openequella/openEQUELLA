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


import com.flickr4java.flickr.photos.Photo;
import com.tle.common.Check;
import com.tle.common.searching.SearchResults;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent;
import com.tle.web.sections.equella.search.event.SearchResultsListener;

/**
 * @author larry
 */
public class FlickrSearchResultsEvent extends AbstractSearchResultsEvent<FlickrSearchResultsEvent>
{
	private final SearchResults<Photo> results;
	/**
	 * This variable serves as a boolean: rather than quiz the Flickr service to
	 * determine whether and if so how many items are filtered out by the
	 * query's filter elements, we simply determine if any filter elements have
	 * been set, and if so, set the integer value to Integer.MIN_VALUE, as a
	 * value to represent 'non-zero but unspecified'. Note that such a value
	 * must be a negative number so as not to be included in a catch-all
	 * positive numbers test, eg: x such that x > 0.
	 */
	private int filteredOut = 0;

	/**
	 * The boolean argument preserved in a member variable to flag that some
	 * sort of filter is applied (in addition to any keyword search), and thus
	 * that the result set may have thereby been reduced.
	 * 
	 * @param results
	 * @param filterApplied
	 */
	public FlickrSearchResultsEvent(SearchResults<Photo> results, boolean filterApplied)
	{
		this.results = results;
		if( filterApplied )
		{
			filteredOut = Integer.MIN_VALUE;
		}
		else
		{
			filteredOut = 0;
		}
		String errorMessage = results.getErrorMessage();
		if( !Check.isEmpty(errorMessage) )
		{
			setErrored(true);
			setErrorMessage(errorMessage);
		}
	}

	public SearchResults<Photo> getResults()
	{
		return results;
	}

	/**
	 * @see com.tle.web.sections.events.SectionEvent#fire(com.tle.web.sections.SectionId,
	 *      com.tle.web.sections.SectionInfo, java.util.EventListener)
	 */
	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchResultsListener<FlickrSearchResultsEvent> listener)
		throws Exception
	{
		listener.processResults(info, this);
	}

	/**
	 * @see com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent#getOffset()
	 */
	@Override
	public int getOffset()
	{
		return results.getOffset();
	}

	/**
	 * @see com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent#getCount()
	 */
	@Override
	public int getCount()
	{
		return results.getCount();
	}

	/**
	 * @see com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent#getMaximumResults()
	 */
	@Override
	public int getMaximumResults()
	{
		return results.getAvailable();
	}

	/**
	 * @see com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent#getFilteredOut()
	 */
	@Override
	public int getFilteredOut()
	{
		return filteredOut;
	}
}
