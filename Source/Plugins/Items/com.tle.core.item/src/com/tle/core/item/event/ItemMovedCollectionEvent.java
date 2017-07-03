package com.tle.core.item.event;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.ItemKey;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.item.event.listener.ItemMovedCollectionEventListener;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
public class ItemMovedCollectionEvent extends ApplicationEvent<ItemMovedCollectionEventListener>
{
	private final ItemKey itemId;
	private final String fromCollectionUuid;
	private final String toCollectionUuid;

	public ItemMovedCollectionEvent(ItemKey itemId, String fromCollectionUuid, String toCollectionUuid)
	{
		super(PostTo.POST_TO_ALL_CLUSTER_NODES);
		this.itemId = itemId;
		this.fromCollectionUuid = fromCollectionUuid;
		this.toCollectionUuid = toCollectionUuid;
	}

	@Override
	public Class<ItemMovedCollectionEventListener> getListener()
	{
		return ItemMovedCollectionEventListener.class;
	}

	@Override
	public void postEvent(ItemMovedCollectionEventListener listener)
	{
		listener.itemMovedCollection(this);
	}

	public ItemKey getItemId()
	{
		return itemId;
	}

	public String getFromCollectionUuid()
	{
		return fromCollectionUuid;
	}

	public String getToCollectionUuid()
	{
		return toCollectionUuid;
	}
}
