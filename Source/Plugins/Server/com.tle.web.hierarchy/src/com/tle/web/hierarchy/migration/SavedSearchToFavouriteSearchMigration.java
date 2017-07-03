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

package com.tle.web.hierarchy.migration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.classic.Session;

import com.tle.beans.Institution;
import com.tle.beans.UserPreference;
import com.tle.beans.UserPreference.UserPrefKey;
import com.tle.common.SavedSearch;
import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.core.xml.service.XmlService;

@Bind
public class SavedSearchToFavouriteSearchMigration extends AbstractHibernateSchemaMigration
{
	private static final String migInfo = PluginServiceImpl.getMyPluginId(SavedSearchToFavouriteSearchMigration.class)
		+ ".migration.convertoldsavedsearches.title"; //$NON-NLS-1$

	@Inject
	private XmlService xmlService;
	@Inject
	private SavedSearchConverter savedSearchConverter;

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM UserPreference WHERE key.preferenceID = 'saved.searches'"); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		Query query = session.createQuery(
			"SELECT p.key.institution, p FROM UserPreference p WHERE p.key.preferenceID = 'saved.searches'"); //$NON-NLS-1$
		List<Object[]> savedSearchesPrefs = query.list();

		for( Object[] pair : savedSearchesPrefs )
		{
			Institution inst = new Institution();
			inst.setDatabaseId((Long) pair[0]);
			UserPreference pref = (UserPreference) pair[1];

			Map<String, SavedSearch> searches = xmlService.deserialiseFromXml(getClass().getClassLoader(),
				pref.getData());

			for( SavedSearch ss : searches.values() )
			{
				session.save(savedSearchConverter.convertSavedSearch(inst, pref, ss));
			}
			result.incrementStatus();
		}
		session.flush();
		session.clear();
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getCreationSql(new TablesOnlyFilter("favourite_search")); //$NON-NLS-1$
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FavouriteSearch.class, UserPreference.class, UserPrefKey.class, Institution.class};
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return Collections.singletonList("DELETE FROM user_preference WHERE preferenceid = 'saved.searches'"); //$NON-NLS-1$
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(migInfo);
	}
}
