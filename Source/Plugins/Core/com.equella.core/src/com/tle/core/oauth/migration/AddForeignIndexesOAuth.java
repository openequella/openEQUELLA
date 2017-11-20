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

package com.tle.core.oauth.migration;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hibernate.classic.Session;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import com.tle.beans.Institution;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.oauth.beans.OAuthToken;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.ClassDependencies;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@SuppressWarnings("nls")
@Bind
@Singleton
public class AddForeignIndexesOAuth extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(AddForeignIndexesOAuth.class)
		+ ".migration."; //$NON-NLS-1$

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "addfkeys");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// nothing
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 0;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = Lists.newArrayList();
		sql.addAll(helper.getAddIndexesRaw("oauth_client_permissions", "oac_permissions", "oauth_client_id"));
		sql.addAll(helper.getAddIndexesRaw("oauth_token_permissions", "oat_permissions", "oauth_token_id"));
		return sql;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		Set<Class<?>> clazzes = Sets.newHashSet();
		clazzes.addAll(ClassDependencies.baseEntity());
		clazzes.add(Institution.class);
		clazzes.add(OAuthClient.class);
		clazzes.add(OAuthToken.class);
		return clazzes.toArray(new Class<?>[clazzes.size()]);
	}

}
