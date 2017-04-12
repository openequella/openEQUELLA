package com.tle.web.search.filter;

import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.AbstractQuerySection;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.events.RenderEventContext;

@SuppressWarnings("nls")
@TreeIndexed
public class FilterByKeywordSection extends AbstractQuerySection<Object, FreetextSearchEvent>
	implements
		ResetFiltersListener
{
	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		searchButton.setClickHandler(searchResults.getRestartSearchHandler(tree));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return viewFactory.createResult("filter/filterbykeyword.ftl", context);
	}

	@Override
	protected boolean isIncludeUnfiltered()
	{
		return false;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "fbkw";
	}

	@Override
	public void reset(SectionInfo info)
	{
		queryField.setValue(info, null);
	}
}
