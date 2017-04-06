package com.tle.core.hibernate.factory.guice;

import com.tle.core.config.guice.PropertiesModule;
import com.tle.core.hibernate.ExtendedDialect;
import com.tle.core.hibernate.type.ImmutableHibernateXStreamType;

@SuppressWarnings("nls")
public class HibernateFactoryModule extends PropertiesModule
{
	@Override
	protected String getFilename()
	{
		return "/hibernate.properties";
	}

	@Override
	protected void configure()
	{
		requestStaticInjection(ImmutableHibernateXStreamType.class);
		bindProp("hibernate.connection.driver_class");
		bindNewInstance("hibernate.dialect", ExtendedDialect.class);
		bindProp("hibernate.connection.username");
		bindProp("hibernate.connection.password");
		bindProp("hibernate.connection.url");
	}
}
