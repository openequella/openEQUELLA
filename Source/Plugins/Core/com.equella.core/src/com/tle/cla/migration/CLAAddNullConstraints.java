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

package com.tle.cla.migration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.ExtendedDialect;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.AbstractHibernateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class CLAAddNullConstraints extends AbstractHibernateMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(CLAAddNullConstraints.class) + ".cla.addnulls."; //$NON-NLS-1$

	@SuppressWarnings("nls")
	@Override
	public void migrate(MigrationResult status) throws Exception
	{
		HibernateMigrationHelper helper = createMigrationHelper();
		status.setCanRetry(true);
		List<String> sql = new ArrayList<String>();
		Session session = helper.getFactory().openSession();
		ExtendedDialect extDialect = helper.getExtDialect();
		if( !extDialect.supportsModifyWithConstraints() )
		{
			sql.addAll(helper.getDropConstraintsSQL("cla_portion", "item_id"));
			sql.addAll(helper.getDropConstraintsSQL("cla_section", "portion_id"));
		}
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "cla_portion", "item_id"));
		sql.addAll(helper.getAddNotNullSQLIfRequired(session, "cla_section", "portion_id"));
		if( !extDialect.supportsModifyWithConstraints() )
		{
			sql.addAll(helper.getAddIndexesAndConstraintsForColumns("cla_portion", "item_id"));
			sql.addAll(helper.getAddIndexesAndConstraintsForColumns("cla_section", "portion_id"));
		}
		session.close();
		runSqlStatements(sql, helper.getFactory(), status, AbstractCreateMigration.KEY_STATUS);
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{Portion.class, Section.class};
	}

	@Entity
	@AccessType("field")
	@Table(name = "cla_portion")
	public class Portion
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
		long itemId;
	}

	@Entity
	@AccessType("field")
	@Table(name = "cla_section")
	public class Section
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
		long portionId;
	}

	@SuppressWarnings("nls")
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title", keyPrefix + "description");
	}

}
