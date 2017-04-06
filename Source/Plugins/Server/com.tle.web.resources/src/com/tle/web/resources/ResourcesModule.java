package com.tle.web.resources;

import com.google.inject.AbstractModule;

public class ResourcesModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(ResourcesService.class).asEagerSingleton();
	}

}
