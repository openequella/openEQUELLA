package com.tle.web.endpoint.srw.guice;

import com.google.inject.AbstractModule;
import com.tle.web.endpoint.srw.SRWDatabaseExt;

public class SRWModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		requestStaticInjection(SRWDatabaseExt.class);
	}
}
