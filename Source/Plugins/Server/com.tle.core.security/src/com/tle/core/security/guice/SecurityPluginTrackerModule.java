package com.tle.core.security.guice;

import com.google.inject.name.Names;
import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.security.DomainObjectPrivilegeFilterExtension;

@SuppressWarnings("nls")
public class SecurityPluginTrackerModule extends PluginTrackerModule
{
	@Override
	protected void configure()
	{
		bindTracker(DomainObjectPrivilegeFilterExtension.class, "domainObjectPrivilegeFilter", "bean");
		bindTracker(Object.class, Names.named("domainParam"), "domainObjectParameter", null);
	}
}
