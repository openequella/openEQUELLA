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

package com.tle.web.search.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.queryParser.QueryParser;

import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.collect.Lists;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.Check;
import com.tle.common.search.DefaultSearch;
import com.tle.core.freetext.queries.FreeTextAutocompleteQuery;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.item.service.ItemService;

@Bind(AutoCompleteService.class)
@Singleton
@SuppressWarnings("nls")
public class AutoCompleteServiceImpl implements AutoCompleteService
{
	@Inject
	private BundleCache bundleCache;
	@Inject
	private ItemService itemService;
	@Inject
	private FreeTextService freeTextService;

	@Override
	public AutoCompleteResult[] getAutoCompleteResults(DefaultSearch request, String queryText)
	{
		String query = queryText.replaceAll("[\\*\\~\\?\\\"\\']", "");

		FreeTextQuery existing = request.getFreeTextQuery();
		FreeTextAutocompleteQuery autocomplete = new FreeTextAutocompleteQuery(query);

		if( existing == null )
		{
			request.setFreeTextQuery(autocomplete);
		}
		else
		{
			request.setFreeTextQuery(new FreeTextBooleanQuery(false, true, existing, autocomplete));
		}

		// Get AutoComplete title ids
		try
		{
			List<ItemIdKey> acTitleIds = freeTextService.getAutoCompleteTitles(request);

			Collection<Long> itemNameIds = itemService.getItemNameIds(acTitleIds).values();
			bundleCache.addBundleIds(itemNameIds);
			final Map<Long, String> bundleMap = bundleCache.getBundleMap();

			Collection<AutoCompleteResult> results = null;

			// If empty get AutoComplete term
			if( Check.isEmpty(acTitleIds) )
			{
				results = Lists.newArrayList();
				int lastSpace = query.lastIndexOf(' ');
				String prefix = query.substring(lastSpace + 1, query.length());
				if( !prefix.isEmpty() )
				{
					request.setFreeTextQuery(existing);
					if( lastSpace != -1 )
					{
						request.setQuery(query.substring(0, lastSpace));
					}
					String acTerm = freeTextService.getAutoCompleteTerm(request, prefix);
					if( !Check.isEmpty(acTerm) )
					{
						results.add(buildAutoCompleteResult(acTerm, true));
					}
				}
			}
			else
			{
				results = itemNameIds.stream()
					.map(id -> buildAutoCompleteResult(bundleMap.get(id).toLowerCase(), false))
					.collect(Collectors.toList());
			}
			return results.toArray(new AutoCompleteResult[results.size()]);
		}
		catch( Exception e )
		{
			// We don't want auto complete to blow up ever...
			// if there is an error just return nothing
			return new AutoCompleteResult[0];
		}
	}

	private AutoCompleteResult buildAutoCompleteResult(String value, boolean term)
	{
		return new AutoCompleteResult(value, QueryParser.escape(value), term);
	}
}
