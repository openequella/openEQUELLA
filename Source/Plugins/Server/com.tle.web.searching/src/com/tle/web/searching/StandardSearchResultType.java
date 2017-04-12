package com.tle.web.searching;

import com.tle.common.search.DefaultSearch;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.sections.SectionTree;

public interface StandardSearchResultType
{
	AbstractItemList<StandardItemListEntry, ?> getCustomItemList();

	String getKey();

	String getValue();

	void register(SectionTree tree, String parentId);

	void addResultTypeDefaultRestrictions(DefaultSearch defaultSearch);

	boolean isDisabled();

}
