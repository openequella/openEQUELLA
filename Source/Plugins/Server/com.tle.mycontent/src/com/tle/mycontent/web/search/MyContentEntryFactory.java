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

package com.tle.mycontent.web.search;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.mycontent.service.MyContentService;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.itemlist.item.ItemListEntryFactoryExtension;

@Bind
@Singleton
public class MyContentEntryFactory implements ItemListEntryFactoryExtension
{
	@Inject
	private MyContentService myContentService;
	@Inject
	private Provider<MyContentItemListEntry> entryProvider;

	@Override
	public StandardItemListEntry createItemListEntry(Item item)
	{
		if( myContentService.isMyContentItem(item) )
		{
			return entryProvider.get();
		}
		return null;
	}
}
