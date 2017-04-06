/**
 * 
 */
package com.tle.web.harvesterskipdrmsettings;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

/**
 * @author larry
 */
public class HarvesterSkipDrmSettingsModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/harvesterskipdrmsettings")).toProvider(
			node(RootHarvesterSkipDrmSettingsSection.class));
	}

}
