package com.tle.web.remoting.resteasy;

import java.util.Set;

import javax.ws.rs.core.Application;

import com.google.common.collect.Sets;

public class RestEasyApplication extends Application
{
	protected Set<Class<?>> actualResourceClasses = Sets.newLinkedHashSet();
	protected Set<Object> singletons = Sets.newLinkedHashSet();

	@Override
	public Set<Object> getSingletons()
	{
		return singletons;
	}

	@Override
	public Set<Class<?>> getClasses()
	{
		return actualResourceClasses;
	}
}
