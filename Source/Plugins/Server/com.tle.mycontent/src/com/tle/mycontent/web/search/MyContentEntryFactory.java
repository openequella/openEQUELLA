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
