package com.tle.web.sections.standard.guice;

import javax.inject.Inject;

import com.google.inject.AbstractModule;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.PluginFreemarkerFactory;
import com.tle.web.freemarker.SectionsConfiguration;

public class SectionsStandardModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(FreemarkerFactory.class).to(StandardFreemarkerFactory.class).asEagerSingleton();
	}

	public static class StandardFreemarkerFactory extends PluginFreemarkerFactory
	{
		@Inject
		public StandardFreemarkerFactory(SectionsConfiguration configuration)
		{
			setConfiguration(configuration);
		}
	}
}
