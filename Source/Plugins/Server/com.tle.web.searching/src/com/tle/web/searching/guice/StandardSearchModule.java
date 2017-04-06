package com.tle.web.searching.guice;

import com.tle.web.search.actions.StandardShareSearchQuerySection;
import com.tle.web.search.filter.FilterByDateRangeSection;
import com.tle.web.search.filter.FilterByMimeTypeSection;
import com.tle.web.search.filter.FilterByOwnerSection;
import com.tle.web.search.guice.AbstractSearchModule;
import com.tle.web.search.sort.SortOptionsSection;
import com.tle.web.searching.actions.CriteriaFavouriteSearchAction;
import com.tle.web.searching.section.RootSearchSection;
import com.tle.web.searching.section.SearchQuerySection;
import com.tle.web.searching.section.SearchResultsSection;
import com.tle.web.searching.section.SearchTabsSection;
import com.tle.web.sections.Section;
import com.tle.web.sections.equella.search.PagingSection;
import com.tle.web.selection.section.SelectionSummarySection;

@SuppressWarnings("nls")
public class StandardSearchModule extends AbstractSearchModule
{
	@Override
	public NodeProvider getRootNode()
	{
		return node(RootSearchSection.class);
	}

	@Override
	public NodeProvider getQueryNode()
	{
		return node(SearchQuerySection.class);
	}

	@Override
	public NodeProvider getResultsNode()
	{
		return node(SearchResultsSection.class);
	}

	@Override
	protected String getTreeName()
	{
		return "/searching";
	}

	@Override
	protected void addActions(NodeProvider node)
	{
		node.child(SelectionSummarySection.class);
	}

	@Override
	protected void addQueryActions(NodeProvider node)
	{
		node.child(CriteriaFavouriteSearchAction.class);
		node.child(SearchTabsSection.class);
	}

	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(SortOptionsSection.class);
		node.child(FilterByOwnerSection.class);
		node.child(FilterByDateRangeSection.class);
		node.child(FilterByMimeTypeSection.class);
		node.child(getShareSection());
	}

	@Override
	protected NodeProvider getPagingNode()
	{
		return new NodeProvider(PagingSection.class)
		{
			@Override
			protected void customize(Section section)
			{
				PagingSection<?, ?> ps = (PagingSection<?, ?>) section;
				ps.setSearchAttachments(true);
			}
		};
	}

	protected NodeProvider getShareSection()
	{
		return node(StandardShareSearchQuerySection.class);
	}
}
