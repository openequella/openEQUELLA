package com.tle.core.item.event;

import com.tle.beans.item.ItemIdKey;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.item.event.listener.IndexItemNowListener;

/**
 * @author Nicholas Read
 */
public class IndexItemNowEvent extends ApplicationEvent<IndexItemNowListener>
{
	private static final long serialVersionUID = 1L;
	private final ItemIdKey itemIdKey;

	public IndexItemNowEvent(ItemIdKey key)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.itemIdKey = key;
	}

	public ItemIdKey getItemIdKey()
	{
		return itemIdKey;
	}

	@Override
	public Class<IndexItemNowListener> getListener()
	{
		return IndexItemNowListener.class;
	}

	@Override
	public void postEvent(IndexItemNowListener listener)
	{
		listener.indexItemNowEvent(this);
	}

}
