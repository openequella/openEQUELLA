package com.tle.web.sections.equella.guice;

import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;

public class EquellaModule extends SimpleEquellaModule
{
	@Override
	protected void configure()
	{
		bindFreemarker();
		super.configure();
	}

	protected void bindFreemarker()
	{
		bind(ExtendedFreemarkerFactory.class).asEagerSingleton();
	}
}
