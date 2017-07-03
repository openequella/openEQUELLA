package com.tle.core.schema.event;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.Schema;
import com.tle.core.entity.event.BaseEntityReferencesEvent;
import com.tle.core.schema.event.listener.SchemaReferencesListener;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
public class SchemaReferencesEvent extends BaseEntityReferencesEvent<Schema, SchemaReferencesListener>
{
	private static final long serialVersionUID = 1L;

	public SchemaReferencesEvent(Schema schema)
	{
		super(schema);
	}

	@Override
	public Class<SchemaReferencesListener> getListener()
	{
		return SchemaReferencesListener.class;
	}

	@Override
	public void postEvent(SchemaReferencesListener listener)
	{
		listener.addSchemaReferencingClasses(entity, referencingClasses);
	}
}
