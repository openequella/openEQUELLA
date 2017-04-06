package com.tle.common.search;

import com.tle.beans.item.ItemStatus;

public class LiveItemSearch extends DefaultSearch
{
	private static final long serialVersionUID = 1L;

	public LiveItemSearch()
	{
		setItemStatuses(ItemStatus.LIVE, ItemStatus.REVIEW);
	}
}
