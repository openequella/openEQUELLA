package com.tle.web.sections.equella.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;

public class SimpleEquellaModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bindListener(Matchers.any(), new TypeListener()
		{
			@Override
			public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter)
			{
				PluginResourceHandler.inst().getForClass(type.getRawType());
			}
		});
	}
}
