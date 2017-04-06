package com.tle.core.events;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.core.events.listeners.ItemDefinitionDeletionListener;

/**
 * @author Nicholas Read
 */
public class ItemDefinitionDeletionEvent
	extends
		BaseEntityDeletionEvent<ItemDefinition, ItemDefinitionDeletionListener>
{
	public ItemDefinitionDeletionEvent(ItemDefinition itemDefinition)
	{
		super(itemDefinition);
	}

	@Override
	public Class<ItemDefinitionDeletionListener> getListener()
	{
		return ItemDefinitionDeletionListener.class;
	}

	@Override
	public void postEvent(ItemDefinitionDeletionListener listener)
	{
		listener.removeReferences(entity);
	}
}
