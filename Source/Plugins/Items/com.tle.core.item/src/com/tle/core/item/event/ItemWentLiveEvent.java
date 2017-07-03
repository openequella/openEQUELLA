package com.tle.core.item.event;

import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.item.event.listener.ItemWentLiveListener;

/**
 * @author Andrew Gibb
 */
public class ItemWentLiveEvent extends ApplicationEvent<ItemWentLiveListener>
{
	private static final long serialVersionUID = 1L;
	private final ItemId itemKey;

	public ItemWentLiveEvent(ItemKey key)
	{
		super(PostTo.POST_ONLY_TO_SELF);
		this.itemKey = ItemId.fromKey(key);
	}

	public ItemId getItemId()
	{
		return itemKey;
	}

	@Override
	public Class<ItemWentLiveListener> getListener()
	{
		return ItemWentLiveListener.class;
	}

	@Override
	public void postEvent(ItemWentLiveListener listener)
	{
		listener.itemWentLiveEvent(this);
	}
}
