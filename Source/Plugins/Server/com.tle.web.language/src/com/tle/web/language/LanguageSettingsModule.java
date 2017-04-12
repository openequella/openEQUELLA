/**
 * 
 */
package com.tle.web.language;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

/**
 * @author larry
 */
public class LanguageSettingsModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/language")).toProvider(node(RootLanguageSection.class));
	}

}
