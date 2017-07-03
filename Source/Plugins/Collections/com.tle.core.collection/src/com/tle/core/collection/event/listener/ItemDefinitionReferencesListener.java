package com.tle.core.collection.event.listener;

import java.util.List;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.core.events.listeners.ApplicationListener;

/**
 * @author Aaron
 */
public interface ItemDefinitionReferencesListener extends ApplicationListener
{
	void addItemDefinitionReferencingClasses(ItemDefinition collection, List<Class<?>> classes);
}
