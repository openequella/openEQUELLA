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

package com.tle.freetext;

import java.io.File;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Multimap;
import com.tle.common.Pair;
import com.tle.common.searching.Search;
import com.tle.common.searching.SearchResults;
import com.tle.common.settings.standard.SearchSettings;
import com.tle.core.freetext.index.ItemIndex;
import com.tle.core.freetext.indexer.IndexingExtension;
import com.tle.core.remoting.MatrixResults;
import com.tle.core.services.item.FreetextResult;

import it.uniroma3.mat.extendedset.wrappers.LongSet;

/**
 * @author Nicholas Read
 */
public interface FreetextIndex
{
	void deleteIndexes();

	void indexBatch(List<IndexedItem> batch);

	SearchSettings getSearchSettings();

	Collection<IndexingExtension> getIndexingExtensions();

	File getStopWordsFile();

	String getDefaultOperator();

	File getRootIndexPath();

	/**
	 * @param <T>
	 * @param searchReq
	 * @param start
	 * @param count Use -1 for all
	 * @return
	 */
	<T extends FreetextResult> SearchResults<T> search(Search searchReq, int start, int count);

	LongSet searchBitSet(Search searchReq);

	int count(Search searchReq);

	/**
	 * @return Collection of value/count pairs
	 */
	Multimap<String, Pair<String, Integer>> facetCount(Search search, Collection<String> fields);

	MatrixResults matrixSearch(Search searchRequest, List<String> fields, boolean countOnly);

	ItemIndex<? extends FreetextResult> getIndexer(String indexItem);

	int getSynchroniseMinutes();

	void prepareItemsForIndexing(Collection<IndexedItem> inditems);

	String suggestTerm(Search request, String prefix);
}
