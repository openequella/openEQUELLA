package com.tle.web.mail;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

@SuppressWarnings("nls")
public class MailSettingsModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/mailsettings")).toProvider(
			node(MailSettingsSection.class));
	}
}
