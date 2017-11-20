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
