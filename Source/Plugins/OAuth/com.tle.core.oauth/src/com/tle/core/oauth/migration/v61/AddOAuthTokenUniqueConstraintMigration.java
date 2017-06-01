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

package com.tle.core.oauth.migration.v61;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.classic.Session;

import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.oauth.beans.OAuthToken;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.CurrentDataSource;
import com.tle.core.hibernate.HibernateFactoryService;
import com.tle.core.migration.AbstractHibernateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

/**
 * @author larry
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class AddOAuthTokenUniqueConstraintMigration extends AbstractHibernateMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl
		.getMyPluginId(AddOAuthTokenUniqueConstraintMigration.class) + ".unique.constraint."; //$NON-NLS-1$

	@Inject
	private HibernateFactoryService hibernateService;

	/**
	 * Syntax OK for Oracle, PostGres SqlServer ...
	 */
	@Override
	public void migrate(MigrationResult status) throws Exception
	{
		status.setCanRetry(false);

		runInTransaction(hibernateService.createConfiguration(CurrentDataSource.get(), getDomainClasses())
			.getSessionFactory(), new HibernateCall()
		{



			@Override
			public void run(Session session) throws Exception
			{
				// Most likely unnecessary, but check for and delete any rows duplicated on user_id + client_id
				//@formatter:off
				session.createSQLQuery(
					"Delete From OAuth_Token Where id in (" +
					"	Select Oat.id From OAuth_Token Oat " +
					"	Join OAuth_Token chaff on (" +
					"		oat.client_id = chaff.client_id and oat.user_id = chaff.user_id)" +
					"	Where Oat.id <> chaff.id)"
					).executeUpdate();
				session.createSQLQuery(
					"Alter Table OAuth_Token Add Constraint UNIQ_USERID_CLIENTID Unique(user_id, client_id)")
					.executeUpdate();
				//@formatter:on
			}
		});
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "title", KEY_PREFIX + "description");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{OAuthToken.class, OAuthClient.class, BaseEntity.class, BaseEntity.Attribute.class,
				Institution.class, LanguageBundle.class, LanguageString.class};
	}

}
