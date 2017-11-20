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

package com.tle.core.legacy.initial;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.classic.Session;

import com.tle.beans.Institution;
import com.tle.beans.user.TLEGroup;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.AbstractHibernateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class EnsureForeignKeyIndexes3 extends AbstractHibernateMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(EnsureForeignKeyIndexes3.class) + ".ensurefki3.";

	@Override
	public void migrate(MigrationResult status) throws Exception
	{
		status.setupSubTaskStatus(AbstractCreateMigration.KEY_CALCULATING, 100);
		status.setCanRetry(true);
		HibernateMigrationHelper helper = createMigrationHelper();
		List<String> sql = new ArrayList<String>();
		Session session = helper.getFactory().openSession();

		sql.addAll(helper.getAddIndexesRaw("tlegroup_users", "tleguElem", "element"));

		session.close();
		runSqlStatements(sql, helper.getFactory(), status, AbstractCreateMigration.KEY_STATUS);
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{Institution.class, TLEGroup.class,};
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title", keyPrefix + "description");
	}
}
