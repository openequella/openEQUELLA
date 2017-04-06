package com.tle.common.search;

import java.util.List;

import com.tle.beans.item.ItemIdKey;

public class ItemIdKeySearchResults extends AbstractItemSearchResults<ItemIdKey>
{
	private static final long serialVersionUID = 1L;

	public ItemIdKeySearchResults(List<ItemIdKey> results, int count, int offset, int available)
	{
		super(results, count, offset, available);
	}
}
