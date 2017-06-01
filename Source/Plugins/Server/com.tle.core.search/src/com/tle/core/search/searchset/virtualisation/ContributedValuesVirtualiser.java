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

package com.tle.core.search.searchset.virtualisation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import com.dytech.edge.queries.FreeTextQuery;
import com.google.inject.Inject;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.LiveItemSearch;
import com.tle.common.search.searchset.SearchSet;
import com.tle.core.guice.Bind;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.core.search.searchset.SearchSetService;
import com.tle.freetext.FreetextIndex;

@Bind
@Singleton
public class ContributedValuesVirtualiser implements SearchSetVirtualiser
{
	@Inject
	private FreetextIndex freetext;
	@Inject
	private SearchSetService searchSetService;

	@Override
	public <T> void expandSearchSet(List<VirtualisableAndValue<T>> rv, T obj, SearchSet set,
		Map<String, String> mappedValues, Collection<String> collectionUuids, VirtualisationHelper<T> helper)
	{
		final LocalSearch ls = new LocalSearch(searchSetService.getFreetextQuery(set),
			searchSetService.getSearchClausesNoVirtualisation(set));

		if( !Check.isEmpty(mappedValues) )
		{
			checkAncestorHierarchyVirtualisation(ls, set, mappedValues);
		}

		filterSearchCollections(ls, collectionUuids);

		String virtPath = set.getVirtualisationPath();
		for( Pair<String, Integer> valueCount : freetext.facetCount(ls, Collections.singletonList(virtPath)).get(
			virtPath) )
		{
			final String value = valueCount.getFirst();
			if( !Check.isEmpty(value) )
			{
				rv.add(helper.newVirtualisedPathFromPrototypeForValue(obj, value, valueCount.getSecond()));
			}
		}
	}

	/**
	 * Only if the searchSet is related to hierarchy topics is it possible to
	 * have parents (ie excludes DynamicCollections) and if this hierarchy does
	 * in fact have parents, and those parents have mapped values (which if
	 * extant will correspond to that parent's virtualisation path) add them as
	 * key/values to the search. In other words, the search must remain confined
	 * by the virtualisation path/values of its hierarchical parents
	 */
	private <T> void checkAncestorHierarchyVirtualisation(LocalSearch ls, SearchSet set,
		Map<String, String> mappedValues)
	{
		for( SearchSet parent = set.getParent(); parent != null; parent = parent.getParent() )
		{
			String mappedValue = mappedValues.get(parent.getId());
			if( !Check.isEmpty(mappedValue) )
			{
				// sanity check - we expect the parent to have a virtualisation
				// path if its uuid is in the map
				String parentVirtualisdationPath = parent.getVirtualisationPath();
				if( !Check.isEmpty(parentVirtualisdationPath) )
				{
					ls.addMust(parent.getVirtualisationPath(), mappedValue);
				}
			}
		}
	}

	private void filterSearchCollections(DefaultSearch search, Collection<String> collectionUuids)
	{
		final Collection<String> existing = search.getCollectionUuids();
		if( existing == null )
		{
			search.setCollectionUuids(collectionUuids);
		}
		else
		{
			existing.retainAll(collectionUuids);
		}
	}

	private static class LocalSearch extends LiveItemSearch
	{
		private static final long serialVersionUID = 1L;

		public LocalSearch(String query, FreeTextQuery freeTextQuery)
		{
			setQuery(query);
			setFreeTextQuery(freeTextQuery);
		}
	}
}
