/**
 * 
 */
package com.tle.web.shortcuturls;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

/**
 * @author larry
 */
@SuppressWarnings("nls")
public class ShortcutUrlsSettingsModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/shortcuturlssettings")).toProvider(
			node(RootShortcutUrlsSettingsSection.class));
	}
}
