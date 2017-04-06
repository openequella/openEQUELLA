/**
 * 
 */
package com.tle.web.login;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

/**
 * @author larry
 */
public class LoginSettingsModule extends SectionsModule
{

	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/loginsettings")).toProvider(
			node(RootLoginSettingsSection.class));
	}
}
