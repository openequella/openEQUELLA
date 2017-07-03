package com.tle.core.collection.event.listener;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.core.events.listeners.ApplicationListener;

/**
 * @author Nicholas Read
 */
public interface ItemDefinitionDeletionListener extends ApplicationListener
{
	void removeReferences(ItemDefinition itemDefinition);
}
