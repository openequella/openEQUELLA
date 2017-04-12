package com.tle.web.itemadmin.section;

import java.util.ArrayList;
import java.util.List;

import com.tle.beans.item.ItemStatus;
import com.tle.web.search.filter.FilterByItemStatusSection;

public class ItemAdminFilterByItemStatusSection extends FilterByItemStatusSection
{
	@Override
	protected List<ItemStatus> getStatusList()
	{
		ItemStatus[] statuses = ItemStatus.values();
		List<ItemStatus> list = new ArrayList<ItemStatus>();
		for( ItemStatus status : statuses )
		{
			if( !status.equals(ItemStatus.PERSONAL) )
			{
				list.add(status);
			}
		}
		return list;
	}
}
