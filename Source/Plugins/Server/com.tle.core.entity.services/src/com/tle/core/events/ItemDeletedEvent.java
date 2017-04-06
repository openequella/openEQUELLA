package com.tle.core.events;

import com.tle.beans.item.ItemIdKey;
import com.tle.core.events.listeners.ItemDeletedListener;

/**
 * @author Nicholas Read
 */
public class ItemDeletedEvent extends ApplicationEvent<ItemDeletedListener>
{
	private static final long serialVersionUID = 1L;
	private final ItemIdKey itemKey;

	public ItemDeletedEvent(ItemIdKey key)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.itemKey = key;
	}

	public long getKey()
	{
		return itemKey.getKey();
	}

	public ItemIdKey getItemId()
	{
		return itemKey;
	}

	@Override
	public Class<ItemDeletedListener> getListener()
	{
		return ItemDeletedListener.class;
	}

	@Override
	public void postEvent(ItemDeletedListener listener)
	{
		listener.itemDeletedEvent(this);
	}
}
