package com.tle.web.search.base;

import javax.inject.Inject;

import com.tle.web.itemlist.item.StandardItemList;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;

public abstract class AbstractItemListResultSection<M extends SearchResultsModel>
	extends
		AbstractFreetextResultsSection<StandardItemListEntry, M>
{
	@Inject
	private StandardItemList itemList;

	@Override
	public StandardItemList getItemList(SectionInfo info)
	{
		return itemList;
	}

	@Override
	protected void registerItemList(SectionTree tree, String id)
	{
		tree.registerInnerSection(itemList, id);
	}
}
