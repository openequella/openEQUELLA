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

package com.tle.core.url.migration;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@SuppressWarnings("nls")
@Bind
@Singleton
public class RemoveNonHttpUrlsMigration extends AbstractHibernateDataMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(RemoveNonHttpUrlsMigration.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "migration.removenonhttpurls.title");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		session
			.createQuery(
				"DELETE FROM ItemReferencedUrls WHERE referenced_urls_id IN (FROM ReferencedURL WHERE url NOT LIKE 'http://%' AND url NOT LIKE 'https://%')")
			.executeUpdate();
		session.createQuery("DELETE FROM ReferencedURL WHERE url NOT LIKE 'http://%' AND url NOT LIKE 'https://%'")
			.executeUpdate();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		int ct = count(session
			.createQuery("SELECT COUNT(*) FROM ReferencedURL WHERE url NOT LIKE 'http://%' AND url NOT LIKE 'https://%'"));
		return ct;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeReferencedURL.class, FakeItemReferencedUrls.class};
	}

	@Entity(name = "ReferencedURL")
	@AccessType("field")
	public static class FakeReferencedURL
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Lob
		String url;
	}

	@Entity(name = "ItemReferencedUrls")
	@AccessType("field")
	public static class FakeItemReferencedUrls
	{
		@Id
		long item_id;
		long referenced_urls_id;
	}
}