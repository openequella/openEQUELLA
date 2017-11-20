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

package com.tle.core.oauth.migration.v52;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RemoveExpressionFromOAuthClientMigration extends AbstractHibernateSchemaMigration
{
	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeOAuthClient.class, FakeAccessExpression.class,};
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.oauth.migration.RemoveExpressionFromOAuthClientMigration.title", "");
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		// Nothing to do
		return Collections.emptyList();
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// Nothing to do
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getDropColumnSQL("oauth_client", "users_expression_id");
	}

	@AccessType("field")
	@Entity(name = "OAuthClient")
	public static class FakeOAuthClient
	{
		@Id
		long id;

		@ManyToOne
		FakeAccessExpression usersExpression;
	}

	@AccessType("field")
	@Entity(name = "AccessExpression")
	public static class FakeAccessExpression
	{
		@Id
		long id;
	}
}
