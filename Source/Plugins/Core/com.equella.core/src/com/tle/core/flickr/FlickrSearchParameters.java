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

package com.tle.core.flickr;

import com.flickr4java.flickr.photos.SearchParameters;



/**
 * @author larry A minor extension to the flickr searchParameters class, wherein
 *         we provide a general purpose string for the user identifier, and
 *         determine later if the identifier is a flickr userid (which is a
 *         prerequisite for the flickr search. or a flickr username or flickr
 *         user's email address. If either of the latter, a preliminary flickr
 *         query is required to get the flickr userid.
 */
public class FlickrSearchParameters extends SearchParameters
{
	private String userRawText;
	private String searchRawText;

	private boolean tagsNotText;
	private boolean tagsAll;

	/**
	 * Unvalidated text which identifies the flickr user (either by flickrid,
	 * email or username)
	 * 
	 * @return
	 */
	public String getUserRawText()
	{
		return userRawText;
	}

	public void setUserRawText(String userRawText)
	{
		this.userRawText = userRawText;
	}

	/**
	 * Raw search text which will either be set as the general text, or as tags
	 * only.
	 * 
	 * @return the searchRawText
	 */
	public String getSearchRawText()
	{
		return searchRawText;
	}

	/**
	 * @param searchRawText the searchRawText to set
	 */
	public void setSearchRawText(String searchRawText)
	{
		this.searchRawText = searchRawText;
	}

	/**
	 * By default tagsNotText and tagsAll are false, requires that user
	 * explicitly set (and that EQUELLA correctly records) that a tags-only
	 * search is requested.
	 * 
	 * @return the tags
	 */
	public boolean isTagsNotText()
	{
		return tagsNotText;
	}

	/**
	 * @param tagsNotText the tagsNotText to set
	 */
	public void setTagsNotText(boolean tagsNotText)
	{
		this.tagsNotText = tagsNotText;
	}

	public boolean isTagsAll()
	{
		return tagsAll;
	}

	/**
	 * @param tagsAll the tagsAll to set
	 */
	public void setTagsAll(boolean tagsAll)
	{
		this.tagsAll = tagsAll;
	}
}
