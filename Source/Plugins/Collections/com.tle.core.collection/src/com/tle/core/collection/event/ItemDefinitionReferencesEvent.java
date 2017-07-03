package com.tle.core.collection.event;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.core.collection.event.listener.ItemDefinitionReferencesListener;
import com.tle.core.entity.event.BaseEntityReferencesEvent;

/**
 * @author Aaron
 */
public class ItemDefinitionReferencesEvent
	extends
		BaseEntityReferencesEvent<ItemDefinition, ItemDefinitionReferencesListener>
{
	public ItemDefinitionReferencesEvent(ItemDefinition entity)
	{
		super(entity);
	}

	@Override
	public Class<ItemDefinitionReferencesListener> getListener()
	{
		return ItemDefinitionReferencesListener.class;
	}

	@Override
	public void postEvent(ItemDefinitionReferencesListener listener)
	{
		listener.addItemDefinitionReferencingClasses(entity, referencingClasses);
	}
}
