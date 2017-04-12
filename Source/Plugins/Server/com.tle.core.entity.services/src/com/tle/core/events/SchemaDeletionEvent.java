package com.tle.core.events;

import com.tle.beans.entity.Schema;
import com.tle.core.events.listeners.SchemaDeletionListener;

/**
 * @author Nicholas Read
 */
public class SchemaDeletionEvent extends BaseEntityDeletionEvent<Schema, SchemaDeletionListener>
{
	public SchemaDeletionEvent(Schema schema)
	{
		super(schema);
	}

	@Override
	public Class<SchemaDeletionListener> getListener()
	{
		return SchemaDeletionListener.class;
	}

	@Override
	public void postEvent(SchemaDeletionListener listener)
	{
		listener.removeReferences(entity);
	}
}
