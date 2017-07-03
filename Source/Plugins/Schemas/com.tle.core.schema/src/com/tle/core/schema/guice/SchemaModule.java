package com.tle.core.schema.guice;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.schema.SchemaReferences;
import com.tle.core.schema.extension.SchemaSaveExtension;

@SuppressWarnings("nls")
public class SchemaModule extends PluginTrackerModule
{
	@Override
	protected void configure()
	{
		bindTracker(SchemaReferences.class, "schemaRefs", "bean");
		bindTracker(SchemaSaveExtension.class, "schemaSave", "bean");
	}

}
