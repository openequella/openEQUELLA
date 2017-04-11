package com.tle.core.plugins;

import java.util.Collection;

public interface PluginBeanLocator
{
	<T> T getBean(String beanId);

	<T> T getBeanForType(Class<T> type);

	<T> void autowire(T bean);

	boolean isInitialised();

	<T> Collection<T> getBeansOfType(Class<T> clazz);

	<T> Class<T> loadClass(String name) throws ClassNotFoundException;

}
