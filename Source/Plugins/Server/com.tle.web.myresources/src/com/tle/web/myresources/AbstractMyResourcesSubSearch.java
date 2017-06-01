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

import java.util.List;

import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.sections.SectionTree;

public abstract class AbstractMyResourcesSubSearch implements MyResourcesSubSearch
{
	private final String nameKey;
	private final String value;
	private final int order;
	private boolean shownOnPortal = true;

	public AbstractMyResourcesSubSearch(String nameKey, String value, int order)
	{
		this.nameKey = nameKey;
		this.value = value;
		this.order = order;
	}

	@Override
	public String getNameKey()
	{
		return nameKey;
	}

	@Override
	public String getValue()
	{
		return value;
	}

	@Override
	public int getOrder()
	{
		return order;
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		// nothing
	}

	@Override
	public AbstractItemList<?, ?> getCustomItemList()
	{
		return null;
	}

	@Override
	public boolean isShownOnPortal()
	{
		return shownOnPortal;
	}

	@Override
	public List<MyResourcesSubSubSearch> getSubSearches()
	{
		return null;
	}

	public void setShownOnPortal(boolean shownOnPortal)
	{
		this.shownOnPortal = shownOnPortal;
	}

	@Override
	public boolean canView()
	{
		return true;
	}
}
