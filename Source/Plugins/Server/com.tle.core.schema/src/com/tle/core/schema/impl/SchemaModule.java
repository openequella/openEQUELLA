package com.tle.core.schema.impl;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.schema.SchemaReferences;

@SuppressWarnings("nls")
public class SchemaModule extends PluginTrackerModule
{
	@Override
	protected void configure()
	{
		bindTracker(SchemaReferences.class, "schemaRefs", "bean");
	}

}
