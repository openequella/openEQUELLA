package com.tle.core.security.guice;

import java.lang.reflect.Method;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.AbstractMatcher;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.plugins.PluginService;
import com.tle.core.security.impl.MethodSecurityInteceptor;
import com.tle.core.security.impl.SecurityAttributeSource;

public class SecurityModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		PluginService pluginService = AbstractPluginService.get();
		MethodSecurityInteceptor interceptor = pluginService.getBeanLocator(
			pluginService.getPluginIdForObject(getClass())).getBeanForType(MethodSecurityInteceptor.class);
		final SecurityAttributeSource source = interceptor.getAttributeSource();
		final TargetClassMatcher targetClassMatcher = new TargetClassMatcher();
		bindInterceptor(targetClassMatcher, new AbstractMatcher<Method>()
		{
			@Override
			public boolean matches(Method t)
			{
				if( !t.isSynthetic() )
				{
					return source.getAttribute(t, targetClassMatcher.getTargetClass()) != null;
				}
				return false;
			}
		}, interceptor);
	}

	public static class TargetClassMatcher extends AbstractMatcher<Class<?>>
	{

		private Class<?> targetClass;

		@Override
		public boolean matches(Class<?> t)
		{
			this.targetClass = t;
			return true;
		}

		public Class<?> getTargetClass()
		{
			return targetClass;
		}
	}

}
