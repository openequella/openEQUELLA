package com.tle.core.events;

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.core.events.listeners.ItemDefinitionReferencesListener;

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

	private final List<Class<?>> referencingClasses = Lists.newArrayList();

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

	public List<Class<?>> getReferencingClasses()
	{
		return referencingClasses;
	}
}
