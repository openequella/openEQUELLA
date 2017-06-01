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

package com.tle.freetext;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dytech.edge.common.valuebean.ItemIndexDate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.beans.Institution;

public class CompareDateCollector extends AbstractCompareDateCollector
{
	private final List<IndexedItem> toIndex = Lists.newArrayList();
	private final Map<FullIdKey, ItemIndexDate> timeMap = Maps.newHashMap();
	private final IndexedItemFactory indexedItemProvider;
	private final long ignoreMissingAfter;

	public CompareDateCollector(Collection<ItemIndexDate> modifiedTimes, IndexedItemFactory indexedItemProvider,
		Map<Long, Institution> insts, List<ItemIndexDelete> toDelete, Date ignoreMissingAfter)
	{
		super(insts, toDelete);
		for( ItemIndexDate dkey : modifiedTimes )
		{
			timeMap.put(new FullIdKey(dkey), dkey);
		}
		this.indexedItemProvider = indexedItemProvider;
		this.ignoreMissingAfter = ignoreMissingAfter.getTime();
	}

	@Override
	public void compareDate(long itemId, long instId, long time)
	{
		FullIdKey id = new FullIdKey(itemId, instId);
		ItemIndexDate itemDate = timeMap.remove(id);
		if( itemDate == null || itemDate.getLastIndexed().getTime() != time )
		{
			IndexedItem indItem = null;
			if( itemDate == null )
			{
				// not in database, remove from index
				if( toDelete != null && time < ignoreMissingAfter )
				{
					toDelete.add(new ItemIndexDelete(itemId, instMap.get(instId)));
				}
			}
			else
			{
				// not up to date, reindex
				indItem = indexedItemProvider.create(itemDate.getKey(), instMap.get(itemDate.getInstitutionId()));
				indItem.setAdd(true);
			}
			if( indItem != null )
			{
				indItem.setNewSearcherRequired(true);
				toIndex.add(indItem);
			}
		}

	}

	@Override
	public List<IndexedItem> getModifiedDocs()
	{
		for( ItemIndexDate itemDate : timeMap.values() )
		{
			// not in index at all, just add
			IndexedItem indItem = indexedItemProvider.create(itemDate.getKey(),
				instMap.get(itemDate.getInstitutionId()));
			indItem.setNewSearcherRequired(true);
			indItem.setAdd(true);
			toIndex.add(indItem);
		}
		return toIndex;
	}

}
