package com.tle.core.schema.event.listener;

import java.util.List;

import com.tle.beans.entity.Schema;
import com.tle.core.events.listeners.ApplicationListener;

/**
 * @author Aaron
 *
 */
public interface SchemaReferencesListener extends ApplicationListener
{
	void addSchemaReferencingClasses(Schema schema, List<Class<?>> referencingClasses);
}
