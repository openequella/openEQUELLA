package com.tle.core.events;

import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.core.events.listeners.UpdateReferencedUrlsListener;

/**
 * @author Nicholas Read
 */
public class UpdateReferencedUrlsEvent extends ApplicationEvent<UpdateReferencedUrlsListener>
{
	private static final long serialVersionUID = 1L;
	private ItemId itemKey;

	public UpdateReferencedUrlsEvent(ItemKey itemKey)
	{
		super(PostTo.POST_ONLY_TO_SELF);
		this.itemKey = ItemId.fromKey(itemKey);
	}

	public ItemId getItemKey()
	{
		return itemKey;
	}

	@Override
	public Class<UpdateReferencedUrlsListener> getListener()
	{
		return UpdateReferencedUrlsListener.class;
	}

	@Override
	public void postEvent(UpdateReferencedUrlsListener listener)
	{
		listener.updateReferencedUrlsEvent(this);
	}
}
