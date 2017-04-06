package com.tle.freetext;

import it.uniroma3.mat.extendedset.wrappers.LongSet;

import java.io.File;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Multimap;
import com.tle.beans.system.SearchSettings;
import com.tle.common.Pair;
import com.tle.common.searching.Search;
import com.tle.common.searching.SearchResults;
import com.tle.core.remoting.MatrixResults;
import com.tle.core.services.item.FreetextResult;
import com.tle.freetext.index.ItemIndex;

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
