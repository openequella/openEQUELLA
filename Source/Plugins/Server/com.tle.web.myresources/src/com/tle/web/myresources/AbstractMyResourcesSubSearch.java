package com.tle.web.myresources;

import java.util.List;

import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.sections.SectionTree;

public abstract class AbstractMyResourcesSubSearch implements MyResourcesSubSearch
{
	private final String nameKey;
	private final String value;
	private final int order;
	private boolean shownOnPortal = true;

	public AbstractMyResourcesSubSearch(String nameKey, String value, int order)
	{
		this.nameKey = nameKey;
		this.value = value;
		this.order = order;
	}

	@Override
	public String getNameKey()
	{
		return nameKey;
	}

	@Override
	public String getValue()
	{
		return value;
	}

	@Override
	public int getOrder()
	{
		return order;
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		// nothing
	}

	@Override
	public AbstractItemList<?, ?> getCustomItemList()
	{
		return null;
	}

	@Override
	public boolean isShownOnPortal()
	{
		return shownOnPortal;
	}

	@Override
	public List<MyResourcesSubSubSearch> getSubSearches()
	{
		return null;
	}

	public void setShownOnPortal(boolean shownOnPortal)
	{
		this.shownOnPortal = shownOnPortal;
	}

	@Override
	public boolean canView()
	{
		return true;
	}
}
