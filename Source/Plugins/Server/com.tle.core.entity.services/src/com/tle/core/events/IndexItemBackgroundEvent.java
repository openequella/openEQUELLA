package com.tle.core.events;

import com.tle.beans.item.ItemIdKey;
import com.tle.core.events.listeners.IndexItemBackgroundListener;

/**
 * @author Nicholas Read
 */
public class IndexItemBackgroundEvent extends ApplicationEvent<IndexItemBackgroundListener>
{
	private static final long serialVersionUID = 1L;
	private final ItemIdKey itemIdKey;

	public IndexItemBackgroundEvent(ItemIdKey itemId, boolean self)
	{
		super(self ? PostTo.POST_TO_ALL_CLUSTER_NODES : PostTo.POST_TO_OTHER_CLUSTER_NODES);
		this.itemIdKey = itemId;
	}

	public ItemIdKey getItemIdKey()
	{
		return itemIdKey;
	}

	@Override
	public Class<IndexItemBackgroundListener> getListener()
	{
		return IndexItemBackgroundListener.class;
	}

	@Override
	public void postEvent(IndexItemBackgroundListener listener)
	{
		listener.indexItemBackgroundEvent(this);
	}
}
