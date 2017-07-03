package com.tle.core.item.event;

import com.tle.beans.item.ItemIdKey;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.item.event.listener.UnindexItemListener;

/**
 * @author Nicholas Read
 */
public class UnindexItemEvent extends ApplicationEvent<UnindexItemListener>
{
	private static final long serialVersionUID = 1L;
	private final ItemIdKey itemId;

	public UnindexItemEvent(ItemIdKey itemId, boolean self)
	{
		super(self ? PostTo.POST_TO_SELF_SYNCHRONOUSLY : PostTo.POST_TO_OTHER_CLUSTER_NODES);
		this.itemId = itemId;
	}

	public ItemIdKey getItemId()
	{
		return itemId;
	}

	@Override
	public Class<UnindexItemListener> getListener()
	{
		return UnindexItemListener.class;
	}

	@Override
	public void postEvent(UnindexItemListener listener)
	{
		listener.unindexItemEvent(this);
	}
}
