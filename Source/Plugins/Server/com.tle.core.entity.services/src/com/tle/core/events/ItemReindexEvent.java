package com.tle.core.events;

import com.tle.core.events.listeners.ItemReindexListener;
import com.tle.freetext.reindexing.ReindexFilter;

/**
 * @author Nicholas Read
 */
public class ItemReindexEvent extends ApplicationEvent<ItemReindexListener>
{
	private static final long serialVersionUID = 1L;
	private ReindexFilter filter;

	public ItemReindexEvent(ReindexFilter filter)
	{
		super(PostTo.POST_ONLY_TO_SELF);
		this.filter = filter;
	}

	public ReindexFilter getFilter()
	{
		return filter;
	}

	@Override
	public Class<ItemReindexListener> getListener()
	{
		return ItemReindexListener.class;
	}

	@Override
	public void postEvent(ItemReindexListener listener)
	{
		listener.itemReindexEvent(this);
	}
}
