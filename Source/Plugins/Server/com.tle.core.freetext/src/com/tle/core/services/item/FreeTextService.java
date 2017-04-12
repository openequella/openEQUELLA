package com.tle.core.services.item;

import it.uniroma3.mat.extendedset.wrappers.LongSet;

import java.util.Collection;
import java.util.List;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.searching.Search;
import com.tle.common.searching.SearchResults;
import com.tle.core.remoting.MatrixResults;

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