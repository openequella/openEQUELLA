package com.tle.core.schema.event.listener;

import com.tle.beans.entity.Schema;
import com.tle.core.events.listeners.ApplicationListener;

/**
 * @author Nicholas Read
 */
public interface SchemaDeletionListener extends ApplicationListener
{
	void removeReferences(Schema schema);
}
