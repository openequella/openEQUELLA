package com.tle.web.myresources;

import com.tle.beans.item.ItemStatus;
import com.tle.web.search.filter.FilterByItemStatusSection;
import com.tle.web.sections.SectionInfo;

public class ItemStatusSubSearch extends AbstractMyResourcesSubSearch
{
	private final ItemStatus[] statuses;

	public ItemStatusSubSearch(String nameKey, String value, int order, ItemStatus... itemStatuses)
	{
		super(nameKey, value, order);
		this.statuses = itemStatuses;
	}

	@Override
	public MyResourcesSearch createDefaultSearch(SectionInfo info)
	{
		MyResourcesSearch search = new MyResourcesSearch();
		search.setItemStatuses(statuses);
		return search;
	}

	@Override
	public void setupFilters(SectionInfo info)
	{
		FilterByItemStatusSection status = info.lookupSection(FilterByItemStatusSection.class);
		status.disable(info);
	}

}
