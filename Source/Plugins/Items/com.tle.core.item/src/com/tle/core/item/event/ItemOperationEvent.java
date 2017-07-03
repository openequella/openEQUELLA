package com.tle.core.item.event;

import com.tle.core.events.ApplicationEvent;
import com.tle.core.item.event.listener.ItemOperationListener;
import com.tle.core.item.operations.BaseFilter;
import com.tle.core.plugins.BeanLocator;

/**
 * @author Nicholas Read
 */
public class ItemOperationEvent extends ApplicationEvent<ItemOperationListener>
{
	private static final long serialVersionUID = 1L;
	private final BeanLocator<? extends BaseFilter> locator;

	public ItemOperationEvent(BeanLocator<? extends BaseFilter> locator)
	{
		super(PostTo.POST_ONLY_TO_SELF);
		this.locator = locator;
	}

	public BaseFilter getOperation()
	{
		return locator.get();
	}

	@Override
	public Class<ItemOperationListener> getListener()
	{
		return ItemOperationListener.class;
	}

	@Override
	public void postEvent(ItemOperationListener listener)
	{
		listener.itemOperationEvent(this);
	}
}
