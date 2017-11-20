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
import com.tle.web.itemlist.item.AbstractItemListEntry;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.generic.NumberOrder;

public interface MyResourcesSubSearch extends NumberOrder
{
	MyResourcesSearch createDefaultSearch(SectionInfo info);

	List<MyResourcesSubSubSearch> getSubSearches();

	void setupFilters(SectionInfo info);

	void register(SectionTree tree, String parentId);

	AbstractItemList<? extends AbstractItemListEntry, ?> getCustomItemList();

	String getNameKey();

	String getValue();

	boolean isShownOnPortal();

	boolean canView();
}
