package com.tle.core.favourites.dao;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.core.events.IndexItemBackgroundEvent;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

@Bind
public class UpdateLatestOperation extends AbstractWorkflowOperation
{
	@Inject
	private BookmarkDao dao;

	@Override
	public boolean execute()
	{
		if( params.isWentLive() )
		{
			List<Item> itemsToUpdate = dao.updateAlwaysLatest(getItem());
			for( Item item : itemsToUpdate )
			{
				addAfterCommitEvent(new IndexItemBackgroundEvent(new ItemIdKey(item), true));
			}
		}
		return false;
	}

}
