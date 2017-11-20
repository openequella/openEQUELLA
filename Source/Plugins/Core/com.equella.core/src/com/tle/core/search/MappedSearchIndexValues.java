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

package com.tle.core.search;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tle.beans.item.ItemId;
import com.tle.common.search.DefaultSearch;

/**
 * convenience class for placing search information into the session where it
 * can be referred to by the SearchPrevNextSection, namely a map of index
 * numbers for a search set keyed by the itemIds. The class also includes a
 * miscellaneous data such as the search total available, search offset and the
 * size of the keyResources from a Browse search which serves as an
 * offsetHandicap.
 * 
 * @author larry
 */
@SuppressWarnings("nls")
public class MappedSearchIndexValues implements Serializable
{
	static final long serialVersionUID = 8960156813413948233L;

	/**
	 * The key into the session attributes
	 */
	public static final String MAPPED_SEARCH_ATTR_KEY = "INDEXINTOSEARCH";

	private Map<ItemId, Integer> indexMap;

	private int available;

	private int offset;

	private int offsetHandicap;

	private List<ItemId> keyResourceItemIds;

	private DefaultSearch activeSearch;

	public MappedSearchIndexValues(int available, int offset, int offsetHandicap)
	{
		this.indexMap = new HashMap<ItemId, Integer>();
		this.available = available;
		this.offset = offset;
		this.offsetHandicap = offsetHandicap;
	}

	/**
	 * The index we wish to map is the absolute index across all pages that may
	 * exist in the search results, hence the fourth item on the third page maps
	 * to index value [33] (0-based, and assuming 10 items per page). When
	 * adding items, looping through the items from a page-based search, we'll
	 * want to combine offset and index to get a 'true' index. When adding
	 * individual items when the true index is already known, we'll ignore the
	 * stored offset value and add as given.
	 * 
	 * @param itemId
	 * @param index index which may be relative to a search result page
	 * @param useOffset - whether the index is to be compounded with known
	 *            offset or not
	 */
	public void mapItemIdWithIndex(ItemId itemId, int index, boolean useOffset)
	{
		int absIndex = index;
		if( useOffset )
		{
			absIndex += offset;
		}
		this.indexMap.put(itemId, absIndex);
	}

	/**
	 * @param key
	 * @return -1 index of the itemId key if found in map, else -1
	 */
	public int getIndexForItemId(ItemId key)
	{
		Integer val = indexMap.get(key);
		return val != null ? val.intValue() : -1;
	}

	public Map<ItemId, Integer> getIndexMap()
	{
		return indexMap;
	}

	public int getOffset()
	{
		return offset;
	}

	public int getOffsetHandicap()
	{
		return offsetHandicap;
	}

	public int getAvailable()
	{
		return available;
	}

	public DefaultSearch getActiveSearch()
	{
		return activeSearch;
	}

	public void setActiveSearch(DefaultSearch activeSearch)
	{
		this.activeSearch = activeSearch;
	}

	public List<ItemId> getKeyResourceItemIds()
	{
		return keyResourceItemIds;
	}

	public void setKeyResourceItemIds(List<ItemId> keyResourceItemIds)
	{
		this.keyResourceItemIds = keyResourceItemIds;
	}
}
