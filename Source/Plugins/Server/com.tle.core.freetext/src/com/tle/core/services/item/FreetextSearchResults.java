package com.tle.core.services.item;

import com.tle.beans.item.Item;
import com.tle.common.searching.SearchResults;

public interface FreetextSearchResults<T extends FreetextResult> extends SearchResults<Item>
{
	T getResultData(int index);

	Item getItem(int index);

	int getKeyResourcesSize();
}
