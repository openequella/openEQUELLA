package com.tle.core.services.guice;

import com.google.inject.AbstractModule;
import com.tle.core.hibernate.guice.TransactionModule;
import com.tle.core.security.guice.SecurityModule;

public class ServicesModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		install(new TransactionModule());
		install(new SecurityModule());
	}

}
