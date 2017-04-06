package com.tle.web.freemarker.guice;

import javax.inject.Inject;

import com.google.inject.AbstractModule;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.PluginFreemarkerFactory;
import com.tle.web.freemarker.SectionsConfiguration;

public class FreemarkerModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(FreemarkerFactory.class).to(DefaultFreemarkerFactory.class).asEagerSingleton();
	}

	public static class DefaultFreemarkerFactory extends PluginFreemarkerFactory
	{
		@Inject
		public DefaultFreemarkerFactory(SectionsConfiguration config)
		{
			setConfiguration(config);
		}
	}
}
