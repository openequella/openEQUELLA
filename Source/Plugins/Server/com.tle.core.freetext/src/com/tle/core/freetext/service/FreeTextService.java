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

package com.tle.core.freetext.service;

import it.uniroma3.mat.extendedset.wrappers.LongSet;

import java.util.Collection;
import java.util.List;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.searching.Search;
import com.tle.common.searching.SearchResults;
import com.tle.core.remoting.MatrixResults;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;

/**
 * @author Nicholas Read
 */
public interface FreeTextService
{
	void indexAll();

	/**
	 * @param <T>
	 * @param searchreq
	 * @param nStart
	 * @param nCount Use -1 for all
	 * @return
	 */
	<T extends FreetextResult> FreetextSearchResults<T> search(Search searchreq, int nStart, int nCount);

	SearchResults<ItemIdKey> searchIds(Search searchreq, int nStart, int nCount);

	LongSet searchIdsBitSet(Search searchreq);

	int totalCount(Collection<String> hashFrom, String where);

	List<ItemIdKey> getKeysForNodeValue(String uuid, ItemDefinition itemdef, String node, String value);

	int[] countsFromFilters(Collection<? extends Search> filters);

	List<ItemIdKey> getAutoCompleteTitles(Search request);

	String getAutoCompleteTerm(Search request, String prefix);

	void waitUntilIndexed(ItemIdKey itemIdKey);

	MatrixResults matrixSearch(Search searchRequest, List<String> fields, boolean countOnly, int width);
}