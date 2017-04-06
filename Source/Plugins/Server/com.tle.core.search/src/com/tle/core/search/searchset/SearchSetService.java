package com.tle.core.search.searchset;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tle.common.search.searchset.SearchSet;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.core.search.searchset.virtualisation.VirtualisationHelper;

public interface SearchSetService
{
	<T> List<VirtualisableAndValue<T>> expandSearchSets(Collection<T> objs, Map<String, String> mappedValues,
		Collection<String> collectionUuids, VirtualisationHelper<T> helper);

	String getFreetextQuery(SearchSet topic);

	FreeTextBooleanQuery getSearchClauses(final SearchSet searchSet, Map<String, String> virtualisationValues);

	FreeTextBooleanQuery getSearchClausesNoVirtualisation(final SearchSet searchSet);
}
