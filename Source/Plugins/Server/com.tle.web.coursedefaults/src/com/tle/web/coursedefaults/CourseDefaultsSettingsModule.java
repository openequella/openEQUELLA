/**
 *
 */
package com.tle.web.coursedefaults;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

@SuppressWarnings("nls")
public class CourseDefaultsSettingsModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/coursedefaultssettings")).toProvider(
			node(CourseDefaultsSettingsSection.class));
	}
}
