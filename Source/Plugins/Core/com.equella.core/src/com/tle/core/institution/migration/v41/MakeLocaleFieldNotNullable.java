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

package com.tle.core.institution.migration.v41;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

/**
 * @author aholland
 */
@Bind
@Singleton
public class MakeLocaleFieldNotNullable extends AbstractHibernateSchemaMigration
{
	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1;
	}

	@SuppressWarnings("nls")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// set locale to the server default when there is none
		final Locale locale = Locale.getDefault();
		final String lang = locale.getLanguage();
		session.createQuery("UPDATE LanguageString SET locale = :lang WHERE locale IS NULL").setParameter("lang", lang)
			.executeUpdate();
		result.incrementStatus();
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return null;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeLanguageString.class};
	}

	@SuppressWarnings("nls")
	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		final List<String> sql = new ArrayList<String>();
		sql.add(helper.getDropNamedIndex("language_string", "localeIndex"));
		sql.addAll(helper.getAddNotNullSQL("language_string", "locale"));
		sql.add(helper.getAddNamedIndex("language_string", "localeIndex", "locale"));
		return sql;
	}

	@SuppressWarnings("nls")
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.entity.services.localefield.title");
	}

	@Entity(name = "LanguageString")
	@AccessType("field")
	public static class FakeLanguageString
	{
		@Id
		long id;

		@Column(length = 20)
		@Index(name = "localeIndex")
		String locale;
	}
}
