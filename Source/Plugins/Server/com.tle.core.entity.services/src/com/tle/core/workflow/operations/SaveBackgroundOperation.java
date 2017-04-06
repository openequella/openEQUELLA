package com.tle.core.workflow.operations;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.core.events.IndexItemBackgroundEvent;

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
