package com.tle.core.hibernate.event;

import java.util.Collection;

import com.tle.common.Check;
import com.tle.core.events.ApplicationEvent;

public class SchemaEvent extends ApplicationEvent<SchemaListener>
{
	private static final long serialVersionUID = 1L;

	private Collection<Long> availableSchemas;
	private Collection<Long> unavailableSchemas;
	private boolean systemUp;

	public SchemaEvent(boolean systemUp)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.systemUp = systemUp;
	}

	public SchemaEvent(Collection<Long> availableSchemas, Collection<Long> unavailableSchemas)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.availableSchemas = availableSchemas;
		this.unavailableSchemas = unavailableSchemas;
	}

	@Override
	public void postEvent(SchemaListener listener)
	{
		if( systemUp )
		{
			listener.systemSchemaUp();
		}
		if( !Check.isEmpty(availableSchemas) )
		{
			listener.schemasAvailable(availableSchemas);
		}
		if( !Check.isEmpty(unavailableSchemas) )
		{
			listener.schemasUnavailable(unavailableSchemas);
		}
	}

	@Override
	public Class<SchemaListener> getListener()
	{
		return SchemaListener.class;
	}
}
