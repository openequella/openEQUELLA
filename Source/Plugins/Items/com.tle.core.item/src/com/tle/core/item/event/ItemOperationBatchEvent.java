package com.tle.core.item.event;

import java.util.ArrayList;
import java.util.List;

import com.tle.core.events.ApplicationEvent;
import com.tle.core.item.event.listener.ItemOperationBatchListener;

/**
 * @author Nicholas Read
 */
public class ItemOperationBatchEvent extends ApplicationEvent<ItemOperationBatchListener>
{
	private final List<ItemOperationEvent> events;

	public ItemOperationBatchEvent()
	{
		super(PostTo.POST_ONLY_TO_SELF);
		events = new ArrayList<ItemOperationEvent>();
	}

	public ItemOperationBatchEvent addEvent(ItemOperationEvent event)
	{
		events.add(event);
		return this;
	}

	public List<ItemOperationEvent> getEvents()
	{
		return events;
	}

	@Override
	public Class<ItemOperationBatchListener> getListener()
	{
		return ItemOperationBatchListener.class;
	}

	@Override
	public void postEvent(ItemOperationBatchListener listener)
	{
		listener.itemOperationBatchEvent(this);
	}
}
