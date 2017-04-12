/**
 * 
 */
package com.tle.web.quickcontributeandversion;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

/**
 * @author larry
 */
public class QuickContributeAndVersionSettingsModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/quickcontributeandversionsettings")).toProvider(
			node(RootQuickContributeAndVersionSettingsSection.class));
	}

}
