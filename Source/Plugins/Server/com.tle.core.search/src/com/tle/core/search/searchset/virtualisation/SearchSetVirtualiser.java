package com.tle.core.search.searchset.virtualisation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tle.common.search.searchset.SearchSet;
import com.tle.core.search.VirtualisableAndValue;

public interface SearchSetVirtualiser
{
	<T> void expandSearchSet(List<VirtualisableAndValue<T>> rv, T obj, SearchSet set, Map<String, String> mappedValues,
		Collection<String> collectionUuids, VirtualisationHelper<T> helper);

}
