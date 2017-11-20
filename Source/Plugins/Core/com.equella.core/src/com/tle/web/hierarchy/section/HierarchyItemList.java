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

package com.tle.web.hierarchy.section;

import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.FreetextResult;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.itemlist.item.StandardItemListEntryFactory;
import com.tle.web.sections.SectionInfo;

@Bind
public class HierarchyItemList
	extends
		AbstractItemList<StandardItemListEntry, AbstractItemList.Model<StandardItemListEntry>>
{
	@Inject
	private StandardItemListEntryFactory factory;

	@SuppressWarnings("nls")
	@Override
	protected Set<String> getExtensionTypes()
	{
		return ImmutableSet.of(ItemlikeListEntryExtension.TYPE_STANDARD, "hierarchy");
	}

	@Override
	protected StandardItemListEntry createItemListEntry(SectionInfo info, Item item, FreetextResult result)
	{
		return factory.createItemListItem(info, item, result);
	}
}
