/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
