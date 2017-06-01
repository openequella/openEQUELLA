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

package com.tle.core.workflow.notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.exceptions.NotFoundException;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.core.guice.Bind;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.xstream.XmlService;

@Bind(WorkflowPreferencesService.class)
@Singleton
public class WorkflowPreferencesServiceImpl implements WorkflowPreferencesService
{
	@Inject
	private UserPreferenceService userPreferenceService;
	@Inject
	private XmlService xmlService;
	@Inject
	private ItemDefinitionService itemDefinitionService;

	@Override
	public Multimap<String, String> getWatchedCollectionMap()
	{
		Map<String, String> allWatched = userPreferenceService.getPreferenceForAllUsers(WATCHED_ITEMDEFS);
		Multimap<String, String> colMap = HashMultimap.create();
		for( Entry<String, String> entry : allWatched.entrySet() )
		{
			String pref = entry.getValue();
			String userId = entry.getKey();
			Set<String> colUuids = getCollectionsFromPref(pref);
			for( String colUuid : colUuids )
			{
				colMap.put(colUuid, userId);
			}
		}
		return colMap;
	}

	@Override
	public Set<String> getWatchedCollections()
	{
		String prefs = userPreferenceService.getPreference(WATCHED_ITEMDEFS);
		if( prefs == null )
		{
			return Collections.emptySet();
		}

		return getCollectionsFromPref(prefs);
	}

	private Set<String> getCollectionsFromPref(String prefs)
	{
		Set<String> collections = Sets.newHashSet();
		List<Object> colList = xmlService.deserialiseFromXml(getClass().getClassLoader(), prefs);
		for( Object colId : colList )
		{
			if( colId instanceof Long )
			{
				try
				{
					ItemDefinition collection = itemDefinitionService.get((Long) colId);
					collections.add(collection.getUuid());
				}
				catch( NotFoundException nfe )
				{
					// no longer exists
				}
			}
			else
			{
				collections.add((String) colId);
			}
		}
		return collections;
	}

	@Override
	public void setWatchedCollections(Set<String> watches)
	{
		userPreferenceService
			.setPreference(WATCHED_ITEMDEFS, xmlService.serialiseToXml(new ArrayList<String>(watches)));
	}

}
