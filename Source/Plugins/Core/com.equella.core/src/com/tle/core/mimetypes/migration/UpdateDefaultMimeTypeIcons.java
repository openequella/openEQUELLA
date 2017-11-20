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

package com.tle.core.mimetypes.migration;

import java.io.Serializable;

import javax.inject.Singleton;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.ScrollableResults;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class UpdateDefaultMimeTypeIcons extends AbstractHibernateDataMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(UpdateDefaultMimeTypeIcons.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "mimetypes.migration.title");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		ScrollableResults results = session.createQuery(
			"FROM MimeEntryAttributes WHERE element LIKE '%.gif' AND mapkey = 'PluginIconPath' ").scroll();

		while( results.next() )
		{
			Object[] resultEntry = results.get();
			FakeMimeEntryAttributes fmeAttr = (FakeMimeEntryAttributes) resultEntry[0];
			fmeAttr.element = fmeAttr.element.replaceAll(".gif", ".png");

			session.save(fmeAttr);
			session.flush();
			session.clear();
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM MimeEntryAttributes WHERE element LIKE '%.gif' AND mapkey = 'PluginIconPath' ");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeMimeEntryAttributes.class, FakeMimeEntryAttributesKey.class};
	}

	@Entity(name = "MimeEntryAttributes")
	public static class FakeMimeEntryAttributes implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@Id
		public FakeMimeEntryAttributesKey mimeEntryId;

		@Lob
		String element;
	}

	@Embeddable
	public static class FakeMimeEntryAttributesKey implements Serializable
	{
		private static final long serialVersionUID = 1L;

		long mimeEntryId;

		String mapkey;
	}
}