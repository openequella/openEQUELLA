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

package com.tle.web.viewitem.migration.inplace.v50;

import java.util.Iterator;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.dytech.devlib.PropBagEx;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RemoveViewPackageContentMigration extends AbstractHibernateDataMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(RemoveViewPackageContentMigration.class) + ".";

	private static final String QUERY_WITH_VIEW_CONTENT = "FROM ItemdefBlobs WHERE itemSummarySections LIKE '%viewContentSection%'";

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeItemdefBlobs.class};
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, QUERY_WITH_VIEW_CONTENT);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		final List<FakeItemdefBlobs> idbs = session.createQuery(QUERY_WITH_VIEW_CONTENT).list();
		for( FakeItemdefBlobs idb : idbs )
		{
			final PropBagEx xml = new PropBagEx(idb.getItemSummarySections());

			Iterator<PropBagEx> iter = xml.iterator("configList/com.tle.beans.entity.itemdef.SummarySectionsConfig");
			while( iter.hasNext() )
			{
				if( iter.next().getNode("value").equals("viewContentSection") )
				{
					iter.remove();
				}
			}

			idb.setItemSummarySections(xml.toString());
			session.update(idb);
			session.flush();

			result.incrementStatus();
		}
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "migration.removeviewpackage.title");
	}

	@AccessType("field")
	@Entity(name = "ItemdefBlobs")
	public static class FakeItemdefBlobs
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Lob
		public String itemSummarySections;

		public String getItemSummarySections()
		{
			return itemSummarySections;
		}

		public void setItemSummarySections(String itemSummarySections)
		{
			this.itemSummarySections = itemSummarySections;
		}
	}
}
