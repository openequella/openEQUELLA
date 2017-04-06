package com.tle.web.activation.section;

import java.util.List;

import javax.inject.Inject;

import com.tle.common.search.DefaultSearch;
import com.tle.web.activation.ActivationItemListEntry;
import com.tle.web.activation.ActivationSearch;
import com.tle.web.bulk.section.AbstractBulkSelectionSection;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.event.FreetextSearchResultEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

public class ActivationResultsSection
	extends
		AbstractFreetextResultsSection<ActivationItemListEntry, SearchResultsModel>
{
	@Inject
	private ActivationItemList itemList;

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
		return new ActivationSearch();
	}

	@Override
	public ActivationItemList getItemList(SectionInfo info)
	{
		return itemList;
	}

	@Override
	protected void registerItemList(SectionTree tree, String id)
	{
		tree.registerInnerSection(itemList, id);
	}

	@Override
	protected void addAjaxUpdateDivs(SectionTree tree, List<String> ajaxList)
	{
		super.addAjaxUpdateDivs(tree, ajaxList);
		ajaxList.add(AbstractBulkSelectionSection.DIVID_SELECTBOX);
	}
}
