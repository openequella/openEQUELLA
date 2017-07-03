package com.tle.core.item.standard.operations;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.core.item.event.IndexItemBackgroundEvent;
import com.tle.core.item.event.IndexItemNowEvent;
import com.tle.core.item.event.WaitForItemIndexEvent;

public class ReindexOnlyOperation extends AbstractStandardWorkflowOperation
{
	private final boolean wait;

	@AssistedInject
	protected ReindexOnlyOperation(@Assisted("wait") boolean wait)
	{
		this.wait = wait;
	}

	@Override
	public boolean execute()
	{
		Item item = getItem();
		item.setDateForIndex(params.getDateNow());
		ItemIdKey idKey = params.getItemIdKey();
		addAfterCommitEvent(new IndexItemNowEvent(idKey));
		addAfterCommitEvent(new IndexItemBackgroundEvent(idKey, false));
		if( wait )
		{
			addAfterCommitEvent(new WaitForItemIndexEvent(idKey));
		}
		return false;
	}

}
