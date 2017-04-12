package com.tle.core.events.listeners;

import com.tle.beans.entity.Schema;

/**
 * @author Nicholas Read
 */
public interface SchemaDeletionListener extends ApplicationListener
{
	void removeReferences(Schema schema);
}
