package com.tle.core.plugins;

import java.util.List;
import java.util.Set;

public interface PrivatePluginBeanLocator extends PluginBeanLocator
{
	void clearCallable();

	void setThrowable(Throwable e);

	void addCallables(List<AbstractBeanLocatorCallable<?>> callableList, Set<PrivatePluginBeanLocator> seenLocators);

	boolean isErrored();

}
