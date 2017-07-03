package com.tle.core.institution.migration;

import java.util.Map;
import java.util.Objects;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ConfigPropertyChange implements PostReadMigrator<Map<String, String>>
{
	@Override
	public void migrate(Map<String, String> allProperties)
	{
		String source = allProperties.get("usermanagement.currentSource");
		if( Objects.equals(source, "com.tle.plugins.ump.TLE") )
		{
			allProperties.put("wrapper.user.enabled", "true");
			allProperties.put("wrapper.group.enabled", "true");
			allProperties.put("wrapper.role.enabled", "true");
		}
		else if( Objects.equals(source, "com.tle.plugins.ump.LDAPUserPlugin") )
		{
			allProperties.put("ldap.enabled", "true");
		}
		else if( Objects.equals(source, "com.tle.plugins.ump.ReplicatedUserPlugin") )
		{
			allProperties.put("replicated.enabled", "true");
		}
	}
}
