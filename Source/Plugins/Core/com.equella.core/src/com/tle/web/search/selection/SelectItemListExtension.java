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

package com.tle.web.search.selection;

import javax.inject.Inject;

import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.web.itemlist.item.ItemListEntry;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;

@Bind
public class SelectItemListExtension extends AbstractSelectItemListExtension<Item, ItemListEntry>
{
	@Inject
	private ViewableItemFactory vitemFactory;

	@Override
	protected ViewableItem<Item> getViewableItem(SectionInfo info, Item item)
	{
		return vitemFactory.createNewViewableItem(item.getItemId());
	}

	@Nullable
	@Override
	public String getItemExtensionType()
	{
		return null;
	}
}
