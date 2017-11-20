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

package com.tle.core.migration;

import java.util.List;

import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;

public abstract class AbstractCreateMigration extends AbstractHibernateMigration
{
	public static final String KEY_STATUS = "com.tle.core.migration.migration.sqlstatus"; //$NON-NLS-1$
	public static final String KEY_CALCULATING = "com.tle.core.migration.migration.sqlcalc"; //$NON-NLS-1$

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	public void migrate(MigrationResult result) throws Exception
	{
		result.setupSubTaskStatus(getCalcKey(), 100);
		result.setCanRetry(true);
		HibernateMigrationHelper helper = createMigrationHelper();
		List<String> createStatements = helper.getCreationSql(getFilter(helper));
		addExtraStatements(helper, createStatements);
		runSqlStatements(createStatements, helper.getFactory(), result, getStatusKey());
	}

	protected void addExtraStatements(HibernateMigrationHelper helper, List<String> statements)
	{
		// nothing
	}

	protected String getStatusKey()
	{
		return KEY_STATUS;
	}

	protected String getCalcKey()
	{
		return KEY_CALCULATING;
	}

	protected abstract HibernateCreationFilter getFilter(HibernateMigrationHelper helper);
}
