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

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.tle.beans.Institution;

public class DatesBeforeCollector extends AbstractCompareDateCollector
{
	private long before;

	public DatesBeforeCollector(Map<Long, Institution> instMap, List<ItemIndexDelete> toDelete, Date before)
	{
		super(instMap, toDelete);
		this.before = before.getTime();
	}

	@Override
	public void compareDate(long itemId, long instId, long time)
	{
		if( time < before )
		{
			toDelete.add(new ItemIndexDelete(itemId, instMap.get(instId)));
		}
	}

	@Override
	public List<IndexedItem> getModifiedDocs()
	{
		return null;
	}

}
