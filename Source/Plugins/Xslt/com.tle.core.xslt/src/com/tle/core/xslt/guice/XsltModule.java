package com.tle.core.xslt.guice;

import com.google.inject.AbstractModule;
import com.tle.core.xslt.ext.Users;

/**
 * @author Aaron
 *
 */
public class XsltModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		requestStaticInjection(Users.class);
	}
}
