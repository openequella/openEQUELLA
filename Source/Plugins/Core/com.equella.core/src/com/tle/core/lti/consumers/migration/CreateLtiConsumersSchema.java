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

package com.tle.core.lti.consumers.migration;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import com.tle.beans.Institution;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.common.lti.consumers.entity.LtiConsumerCustomRole;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.ClassDependencies;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class CreateLtiConsumersSchema extends AbstractCreateMigration
{
	private static final String KEY_PFX = PluginServiceImpl.getMyPluginId(CreateLtiConsumersSchema.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PFX + "lti.migration.createentity");
	}

	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		Set<String> tables = new HashSet<String>();
		tables.add("lti_consumer");
		tables.add("lti_consumer_instructor_roles");
		tables.add("lti_consumer_other_roles");
		tables.add("lti_consumer_unknown_groups");
		tables.add("lti_consumer_custom_role");
		return new TablesOnlyFilter(tables.toArray(new String[tables.size()]));
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		final Set<Class<?>> domainClasses = Sets.newHashSet(ClassDependencies.baseEntity());
		domainClasses.add(Institution.class);
		domainClasses.add(LtiConsumer.class);
		domainClasses.add(LtiConsumerCustomRole.class);
		return domainClasses.toArray(new Class<?>[domainClasses.size()]);
	}

}
