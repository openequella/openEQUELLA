package com.tle.core.item.standard.operations;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.core.item.event.IndexItemBackgroundEvent;

public class SaveBackgroundOperation extends SaveOperation
{
	public SaveBackgroundOperation()
	{
		super(true);
	}

	@Override
	protected void addIndexingEvents(ItemIdKey newKey, Item item)
	{
		addAfterCommitEvent(new IndexItemBackgroundEvent(newKey, true));
	}
}
