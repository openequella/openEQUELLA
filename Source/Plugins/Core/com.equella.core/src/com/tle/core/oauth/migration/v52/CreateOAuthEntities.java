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

import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.oauth.beans.OAuthToken;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class CreateOAuthEntities extends AbstractCreateMigration
{
	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		Set<String> tables = new HashSet<String>();
		tables.add("oauth_client");
		tables.add("oauth_client_permissions");
		tables.add("oauth_token");
		tables.add("oauth_token_permissions");

		return new TablesOnlyFilter(tables.toArray(new String[tables.size()]));
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{OAuthClient.class, BaseEntity.class, BaseEntity.Attribute.class, LanguageBundle.class,
				Institution.class, LanguageString.class, OAuthToken.class,};
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.oauth.migration.oauthentities.title", "");
	}
}
