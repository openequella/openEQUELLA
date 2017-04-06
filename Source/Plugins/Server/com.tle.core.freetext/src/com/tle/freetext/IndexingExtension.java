package com.tle.freetext;

import java.util.List;

import com.tle.beans.item.ItemSelect;

public interface IndexingExtension
{
	void indexFast(IndexedItem indexedItem);

	void indexSlow(IndexedItem indexedItem);

	void prepareForLoad(ItemSelect select);

	void loadForIndexing(List<IndexedItem> items);
}
