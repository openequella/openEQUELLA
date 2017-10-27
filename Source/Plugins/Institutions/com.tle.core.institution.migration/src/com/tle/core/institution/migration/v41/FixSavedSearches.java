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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.beans.UserPreference;
import com.tle.beans.UserPreference.UserPrefKey;
import com.tle.common.Check;
import com.tle.common.SavedSearch;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.xml.service.XmlService;

/**
 * @author aholland
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class FixSavedSearches extends AbstractHibernateDataMigration
{
	private static final int BATCH_SIZE = 1000;

	@Inject
	private XmlService xmlService;

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM UserPreference WHERE key.preferenceID = 'saved.searches'");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		final List<UserPreference> savedSearchPrefs = session
			.createQuery("FROM UserPreference WHERE key.preferenceID = 'saved.searches'").list();

		int record = 0;
		for( UserPreference pref : savedSearchPrefs )
		{
			final Map<String, SavedSearch> searches = xmlService.deserialiseFromXml(getClass().getClassLoader(),
				pref.getData());

			FixSavedSearches.convertSavedSearches(searches.values(), new InPlaceSavedSearchFixerIdResolver(session));

			pref.setData(xmlService.serialiseToXml(searches));

			session.update(pref);
			if( record % BATCH_SIZE == 0 )
			{
				session.flush();
				session.clear();
			}

			record++;
			result.incrementStatus();
		}

		session.flush();
		session.clear();
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{UserPreference.class, UserPrefKey.class, FakeBaseEntity.class};
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.entity.services.savedsearches.title");
	}

	@Entity(name = "BaseEntity")
	@AccessType("field")
	public static class FakeBaseEntity
	{
		@Id
		long id;

		@Column(length = 40, nullable = false)
		@Index(name = "uuidIndex")
		String uuid;
	}

	public static void convertSavedSearches(Collection<SavedSearch> savedSearches, SavedSearchFixerIdResolver fixer)
	{
		if( savedSearches != null )
		{
			for( SavedSearch search : savedSearches )
			{
				// replace itemdefs
				final Collection<Long> itemdefIds = search.getItemdefs();
				if( !Check.isEmpty(itemdefIds) )
				{
					// make sure we add to any properly saved collection UUIDs
					final Set<String> collectionUuids = new HashSet<String>();
					final Collection<String> current = search.getCollections();
					if( current != null )
					{
						collectionUuids.addAll(current);
					}

					for( Long itemdefId : itemdefIds )
					{
						String itemdefUuid = fixer.resolveToCollectionUuid(itemdefId);
						if( itemdefUuid != null )
						{
							collectionUuids.add(itemdefUuid);
						}
					}

					search.setCollections(new ArrayList<String>(collectionUuids));
				}
				search.setItemdefs(null);

				// replace advanced search (if and only if there is currently no
				// powersearchUuid
				if( search.getPowersearchUuid() == null )
				{
					final long powerId = search.getPowersearch();
					if( powerId != 0 )
					{
						search.setPowersearchUuid(fixer.resolveToPowerUuid(powerId));
					}
				}
				search.setPowersearch(0);
			}
		}
	}

	public interface SavedSearchFixerIdResolver
	{
		String resolveToCollectionUuid(Long guided);

		String resolveToPowerUuid(Long power);
	}

	protected static class InPlaceSavedSearchFixerIdResolver implements SavedSearchFixerIdResolver
	{
		private final Query entityQuery;

		protected InPlaceSavedSearchFixerIdResolver(Session session)
		{
			entityQuery = session.createQuery("FROM BaseEntity WHERE id = :id");
		}

		@Override
		public String resolveToCollectionUuid(Long entId)
		{
			final FakeBaseEntity ent = (FakeBaseEntity) entityQuery.setParameter("id", entId).uniqueResult();
			if( ent != null )
			{
				return ent.uuid;
			}
			return null;
		}

		@Override
		public String resolveToPowerUuid(Long entId)
		{
			return resolveToCollectionUuid(entId);
		}
	}
}
