package com.tle.core.services.guice;

import com.google.inject.TypeLiteral;
import com.tle.beans.entity.BaseEntity;
import com.tle.common.item.AbstractHelper;
import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.institution.migration.ItemXmlMigrator;
import com.tle.core.institution.migration.Migrator;
import com.tle.core.services.entity.AbstractEntityService;

public class EntityServicesTrackerModule extends PluginTrackerModule
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
		bindTracker(AbstractHelper.class, "itemHelpers", "bean").orderByParameter("order");

		bindTracker(Migrator.class, "xmlmigration", "bean").setIdParam("id").orderByParameter("date", false, true);
		bindTracker(ItemXmlMigrator.class, "itemxmlmigration", "bean").setIdParam("id").orderByParameter("date", false,
			true);
	}
}
