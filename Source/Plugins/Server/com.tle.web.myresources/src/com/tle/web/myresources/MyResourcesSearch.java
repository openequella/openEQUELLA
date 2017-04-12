package com.tle.web.myresources;

import java.util.List;

import com.tle.beans.item.ItemStatus;
import com.tle.common.search.DefaultSearch;
import com.tle.core.user.CurrentUser;

public class MyResourcesSearch extends DefaultSearch
{
	private static final long serialVersionUID = 1L;

	public MyResourcesSearch()
	{
		setNotItemStatuses(ItemStatus.DELETED);
	}

	@Override
	public void setItemStatuses(List<ItemStatus> itemStatuses)
	{
		super.setItemStatuses(itemStatuses);
		setNotItemStatuses();
	}

	@Override
	public String getOwner()
	{
		return CurrentUser.getUserID();
	}
}
