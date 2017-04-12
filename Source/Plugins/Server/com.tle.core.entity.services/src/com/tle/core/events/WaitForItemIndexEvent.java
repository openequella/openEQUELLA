package com.tle.core.events;

import com.tle.beans.item.ItemIdKey;
import com.tle.core.events.listeners.WaitForItemIndexListener;

/**
 * @author Nicholas Read
 */
public class WaitForItemIndexEvent extends ApplicationEvent<WaitForItemIndexListener>
{
	private static final long serialVersionUID = 1L;
	private ItemIdKey itemIdKey;

	public WaitForItemIndexEvent(ItemIdKey itemIdKey)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.itemIdKey = itemIdKey;
	}

	public ItemIdKey getItemIdKey()
	{
		return itemIdKey;
	}

	@Override
	public Class<WaitForItemIndexListener> getListener()
	{
		return WaitForItemIndexListener.class;
	}

	@Override
	public void postEvent(WaitForItemIndexListener listener)
	{
		listener.waitForItem(this);
	}

}
