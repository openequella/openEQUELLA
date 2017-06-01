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

package com.tle.web.myresources;

import com.tle.beans.item.ItemStatus;
import com.tle.web.search.filter.FilterByItemStatusSection;
import com.tle.web.sections.SectionInfo;

public class ItemStatusSubSearch extends AbstractMyResourcesSubSearch
{
	private final ItemStatus[] statuses;

	public ItemStatusSubSearch(String nameKey, String value, int order, ItemStatus... itemStatuses)
	{
		super(nameKey, value, order);
		this.statuses = itemStatuses;
	}

	@Override
	public MyResourcesSearch createDefaultSearch(SectionInfo info)
	{
		MyResourcesSearch search = new MyResourcesSearch();
		search.setItemStatuses(statuses);
		return search;
	}

	@Override
	public void setupFilters(SectionInfo info)
	{
		FilterByItemStatusSection status = info.lookupSection(FilterByItemStatusSection.class);
		status.disable(info);
	}

}
