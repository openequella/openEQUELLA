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
import com.tle.core.guice.Bind;
import com.tle.core.services.item.FreeTextService;
import com.tle.core.services.item.ItemService;
import com.tle.web.i18n.BundleCache;

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
