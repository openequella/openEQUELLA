package com.tle.core.guice;

import java.util.Set;

import com.tle.core.plugins.PluginService;

public interface BeanChecker
{
	void check(PluginService pluginService, Class<?> actualClass, Set<Class<?>> interfaces);
}
