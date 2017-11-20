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

package com.tle.core.search.searchset.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import com.dytech.common.GeneralConstants;
import com.dytech.edge.queries.FreeTextQuery;
import com.google.inject.Inject;
import com.tle.beans.EntityScript;
import com.tle.beans.ItemDefinitionScript;
import com.tle.beans.SchemaScript;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.Check;
import com.tle.common.search.searchset.SearchSet;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.freetext.queries.FreeTextFieldQuery;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.search.QueryGatherer;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.core.search.searchset.SearchSetService;
import com.tle.core.search.searchset.virtualisation.SearchSetVirtualiser;
import com.tle.core.search.searchset.virtualisation.VirtualisationHelper;

@Bind(SearchSetService.class)
@Singleton
@SuppressWarnings("nls")
public class SearchSetServiceImpl implements SearchSetService
{
	private PluginTracker<SearchSetVirtualiser> virtualisers;

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		virtualisers = new PluginTracker<SearchSetVirtualiser>(pluginService, "com.tle.core.search",
			"searchSetVirtualiser", PluginTracker.LOCAL_ID_FOR_KEY);
		virtualisers.setBeanKey("class");
	}

	@Override
	public <T> List<VirtualisableAndValue<T>> expandSearchSets(Collection<T> objs, Map<String, String> mappedValues,
		Collection<String> collectionUuids, VirtualisationHelper<T> helper)
	{
		List<VirtualisableAndValue<T>> virtualisedPathList = new ArrayList<VirtualisableAndValue<T>>();
		for( T ht : objs )
		{
			SearchSet searchSet = helper.getSearchSet(ht);
			SearchSetVirtualiser virtualiser = getVirtualiser(searchSet);
			if( virtualiser == null )
			{
				VirtualisableAndValue<T> untallied = new VirtualisableAndValue<T>(ht);
				untallied.setCount(GeneralConstants.UNCALCULATED);
				virtualisedPathList.add(untallied);
			}
			else
			{
				virtualiser.expandSearchSet(virtualisedPathList, ht, searchSet, mappedValues, collectionUuids, helper);
			}
		}
		return virtualisedPathList;
	}

	private SearchSetVirtualiser getVirtualiser(SearchSet set)
	{
		return virtualisers.getBeanMap().get(set.getVirtualiserPluginId());
	}

	@Override
	public String getFreetextQuery(SearchSet searchSet)
	{
		QueryGatherer query = new QueryGatherer(true);
		while( searchSet != null )
		{
			query.add(searchSet.getFreetextQuery());
			searchSet = searchSet.isInheritFreetext() ? searchSet.getParent() : null;
		}
		return query.toString();
	}

	@Override
	public FreeTextBooleanQuery getSearchClauses(final SearchSet searchSet,
		final Map<String, String> virtualisationValues)
	{
		final Gatherer itemDefGatherer = new Gatherer();
		final Gatherer schemaGatherer = new Gatherer();

		// We need to AND together the above query with any virtualisation
		// queries for the current set and any parents.
		FreeTextBooleanQuery andQuery = new FreeTextBooleanQuery(false, true);
		andQuery.add(getSearchClauses(searchSet, itemDefGatherer, schemaGatherer));

		SearchSet s = searchSet;
		while( s != null )
		{
			applyVirtualiser(andQuery, s, virtualisationValues);
			s = s.getParent();
		}

		return andQuery;
	}

	private void applyVirtualiser(FreeTextBooleanQuery andQuery, SearchSet searchSet, Map<String, String> values)
	{
		final SearchSetVirtualiser virtualiser = getVirtualiser(searchSet);

		String value = Check.isEmpty(values) ? null : values.get(searchSet.getId());
		if( Check.isEmpty(value) )
		{
			if( virtualiser != null )
			{
				// Sanity check!
				throw new RuntimeException(
					"Error during virtualisation: value must be specified for virtualised search sets");
			}
		}
		else
		{
			if( virtualiser == null )
			{
				// Sanity check!
				throw new RuntimeException(
					"Error during virtualisation: value must not be specified for non-virtualised search sets");
			}

			String virtualisationPath = searchSet.getVirtualisationPath();
			if( virtualisationPath == null )
			{
				throw new RuntimeException(
					"Error during virtualisation: schema node must be specified for virtualised search sets");
			}
			andQuery.add(new FreeTextFieldQuery(virtualisationPath, value));
		}
	}

	@Override
	public FreeTextBooleanQuery getSearchClausesNoVirtualisation(SearchSet searchSet)
	{
		final Gatherer itemDefGatherer = new Gatherer();
		final Gatherer schemaGatherer = new Gatherer();
		return getSearchClauses(searchSet, itemDefGatherer, schemaGatherer);
	}

	private FreeTextBooleanQuery getSearchClauses(final SearchSet searchSet, Gatherer itemDefGatherer,
		Gatherer schemaGatherer)
	{
		// Add any item definitions and schema added specifically to this set
		itemDefGatherer.addAll(searchSet.getItemDefs());
		schemaGatherer.addAll(searchSet.getSchemas());

		final SearchSet parent = searchSet.getParent();

		// Add enabled inherited item definitions
		final List<ItemDefinitionScript> inheritedItemDefs = searchSet.getInheritedItemDefs();
		if( !Check.isEmpty(inheritedItemDefs) )
		{
			for( EntityScript<ItemDefinition> query : inheritedItemDefs )
			{
				if( getSearchClauses(itemDefGatherer, parent, query.getEntity()) )
				{
					itemDefGatherer.add(query);
				}
			}
		}

		// Add enabled inherited schemas
		final List<SchemaScript> inheritedSchemas = searchSet.getInheritedSchemas();
		if( !Check.isEmpty(inheritedSchemas) )
		{
			for( EntityScript<Schema> query : inheritedSchemas )
			{
				if( getSearchClauses(schemaGatherer, parent, query.getEntity()) )
				{
					schemaGatherer.add(query);
				}
			}
		}

		FreeTextBooleanQuery orQuery = new FreeTextBooleanQuery(false, false);
		schemaGatherer.addToQuery(orQuery, FreeTextQuery.FIELD_SCHEMAID);
		itemDefGatherer.addToQuery(orQuery, FreeTextQuery.FIELD_ITEMDEFID);
		return orQuery;
	}

	/**
	 * Gathers the schemas and where clauses for the topic, and recurses on the
	 * parent.
	 */
	private boolean getSearchClauses(Gatherer gatherer, SearchSet topic, Schema entity)
	{
		boolean found = false;
		if( topic != null )
		{
			EntityScript<Schema> added = getQuery(topic.getSchemas(), entity);
			if( added != null )
			{
				found = true;
				gatherer.add(added);
			}

			EntityScript<Schema> inherit = getQuery(topic.getInheritedSchemas(), entity);
			if( inherit != null )
			{
				boolean foundInParent = getSearchClauses(gatherer, topic.getParent(), entity);
				if( foundInParent )
				{
					found = true;
					gatherer.add(added);
				}
			}
		}
		return found;
	}

	/**
	 * Gathers the item definitions and where clauses for the topic, and
	 * recurses on the parent.
	 */
	private boolean getSearchClauses(Gatherer gatherer, SearchSet set, ItemDefinition entity)
	{
		boolean found = false;
		if( set != null )
		{
			EntityScript<ItemDefinition> added = getQuery(set.getItemDefs(), entity);
			if( added != null )
			{
				found = true;
				gatherer.add(added);
			}

			EntityScript<ItemDefinition> inherit = getQuery(set.getInheritedItemDefs(), entity);
			if( inherit != null )
			{
				boolean foundInParent = getSearchClauses(gatherer, set.getParent(), entity);
				if( foundInParent )
				{
					found = true;
					gatherer.add(inherit);
				}
			}
		}
		return found;
	}

	/**
	 * Returns the EntityScript for a given entity from a list of EntityScripts.
	 */
	private <T extends BaseEntity, U extends EntityScript<T>> U getQuery(List<U> queries, T entity)
	{
		for( U query : queries )
		{
			if( entity.equals(query.getEntity()) )
			{
				return query;
			}
		}
		return null;
	}
}
