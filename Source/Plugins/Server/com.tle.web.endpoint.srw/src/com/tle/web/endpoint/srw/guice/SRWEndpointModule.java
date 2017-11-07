package com.tle.web.endpoint.srw.guice;

import com.google.inject.AbstractModule;
import com.tle.web.endpoint.srw.EquellaSRWDatabase;

public class SRWEndpointModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		requestStaticInjection(EquellaSRWDatabase.class);
	}
}
