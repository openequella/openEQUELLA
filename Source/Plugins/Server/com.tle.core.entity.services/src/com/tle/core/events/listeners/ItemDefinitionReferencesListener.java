package com.tle.core.events.listeners;

import java.util.List;

import com.tle.beans.entity.itemdef.ItemDefinition;

/**
 * @author Aaron
 */
public interface ItemDefinitionReferencesListener extends ApplicationListener
{
	void addItemDefinitionReferencingClasses(ItemDefinition collection, List<Class<?>> classes);
}
