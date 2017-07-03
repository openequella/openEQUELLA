package com.tle.core.institution.convert.guice;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.institution.convert.Converter;
import com.tle.core.institution.convert.ItemXmlMigrator;
import com.tle.core.institution.convert.Migrator;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.institution.convert.extension.InstitutionInfoInitialiser;

/**
 * @author Aaron
 *
 */
public class InstitutionConverterTrackerModule extends PluginTrackerModule
{
	@Override
	protected void configure()
	{
		bindTracker(Migrator.class, "xmlmigration", "bean").setIdParam("id").orderByParameter("date", false, true);
		bindTracker(ItemXmlMigrator.class, "itemxmlmigration", "bean").setIdParam("id").orderByParameter("date", false,
			true);
		bindTracker(Converter.class, "converter", "class").setIdParam("id").orderByParameter("order");
		bindTracker(PostReadMigrator.class, "postreadmigration", "bean").setIdParam("id");
		bindTracker(InstitutionInfoInitialiser.class, "institutionInfoInitialiser", "bean");
	}
}
