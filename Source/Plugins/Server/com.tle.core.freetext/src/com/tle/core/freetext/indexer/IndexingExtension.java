package com.tle.core.freetext.indexer;

import java.util.List;

import com.tle.beans.item.ItemSelect;
import com.tle.freetext.IndexedItem;

public interface IndexingExtension
{
	void indexFast(IndexedItem indexedItem);

	void indexSlow(IndexedItem indexedItem);

	void prepareForLoad(ItemSelect select);

	void loadForIndexing(List<IndexedItem> items);
}
