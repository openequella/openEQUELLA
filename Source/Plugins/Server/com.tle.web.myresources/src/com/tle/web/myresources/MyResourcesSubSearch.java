package com.tle.web.myresources;

import java.util.List;

import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.itemlist.item.AbstractItemListEntry;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.generic.NumberOrder;

public interface MyResourcesSubSearch extends NumberOrder
{
	MyResourcesSearch createDefaultSearch(SectionInfo info);

	List<MyResourcesSubSubSearch> getSubSearches();

	void setupFilters(SectionInfo info);

	void register(SectionTree tree, String parentId);

	AbstractItemList<? extends AbstractItemListEntry, ?> getCustomItemList();

	String getNameKey();

	String getValue();

	boolean isShownOnPortal();

	boolean canView();
}
