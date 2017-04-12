package com.tle.core.events.listeners;

import com.tle.beans.entity.itemdef.ItemDefinition;

/**
 * @author Nicholas Read
 */
public interface ItemDefinitionDeletionListener extends ApplicationListener
{
	void removeReferences(ItemDefinition itemDefinition);
}
