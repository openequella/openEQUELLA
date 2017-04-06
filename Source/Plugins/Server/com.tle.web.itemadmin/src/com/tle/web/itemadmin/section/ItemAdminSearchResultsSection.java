package com.tle.web.itemadmin.section;

import java.util.List;

import javax.inject.Inject;

import com.tle.common.search.DefaultSearch;
import com.tle.web.bulk.section.AbstractBulkSelectionSection;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.event.FreetextSearchResultEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

public class ItemAdminSearchResultsSection
	extends
		AbstractFreetextResultsSection<StandardItemListEntry, SearchResultsModel>
{
	@Inject
	private ItemAdminItemList itemList;
	@TreeLookup
	private ItemAdminQuerySection querySection;

	@PlugKey("results.title")
	private static Label LABEL_RESULTSTITLE;

	@Override
	protected Label getDefaultResultsTitle(SectionInfo info, FreetextSearchEvent searchEvent,
		FreetextSearchResultEvent resultsEvent)
	{
		return LABEL_RESULTSTITLE;
	}

	@Override
	protected DefaultSearch createDefaultSearch(SectionInfo info)
	{
		return querySection.createDefaultSearch(info);
	}

	@Override
	public ItemAdminItemList getItemList(SectionInfo info)
	{
		return itemList;
	}

	@Override
	protected void addAjaxUpdateDivs(SectionTree tree, List<String> ajaxList)
	{
		super.addAjaxUpdateDivs(tree, ajaxList);
		ajaxList.add(AbstractBulkSelectionSection.DIVID_SELECTBOX);
	}

	@Override
	protected void registerItemList(SectionTree tree, String id)
	{
		tree.registerInnerSection(itemList, id);
	}
}
