package com.tle.core.plugins;

import java.util.Collection;

public interface PrivatePluginService extends PluginService
{

	void ensureBeanLocators(Collection<? extends PrivatePluginBeanLocator> singletonList);

	void setPluginBeanLocator(String extPluginId, PrivatePluginBeanLocator locator);
}
