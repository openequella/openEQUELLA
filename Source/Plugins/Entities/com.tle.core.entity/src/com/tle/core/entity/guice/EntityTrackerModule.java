package com.tle.core.entity.guice;

import com.google.inject.TypeLiteral;
import com.tle.beans.entity.BaseEntity;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.PluginTrackerModule;

public class EntityTrackerModule extends PluginTrackerModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		TypeLiteral<AbstractEntityService<?, BaseEntity>> entityService = new TypeLiteral<AbstractEntityService<?, BaseEntity>>()
		{
			// empty
		};
		bindTracker(entityService.getType(), "entityService", "serviceClass").orderByParameter("order");
	}
}
