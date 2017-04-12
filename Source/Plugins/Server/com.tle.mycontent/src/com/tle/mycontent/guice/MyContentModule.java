package com.tle.mycontent.guice;

import com.google.inject.name.Names;
import com.tle.mycontent.web.section.MyContentContributeSection;
import com.tle.mycontent.web.section.MyContentHandlerDelegationSection;
import com.tle.mycontent.web.selection.MyContentSelectResults;
import com.tle.mycontent.web.selection.MyContentSelectSection;
import com.tle.web.search.filter.FilterByDateRangeSection;
import com.tle.web.search.filter.FilterByKeywordSection;
import com.tle.web.search.filter.FilterByMimeTypeSection;
import com.tle.web.search.guice.AbstractSearchModule;
import com.tle.web.search.sort.SortOptionsSection;
import com.tle.web.selection.section.SelectionSummarySection;

@SuppressWarnings("nls")
public class MyContentModule extends AbstractSearchModule
{
	@Override
	protected void configure()
	{
		super.configure();
		NodeProvider contribute = node(MyContentContributeSection.class);
		contribute.child(MyContentHandlerDelegationSection.class);
		bind(Object.class).annotatedWith(Names.named("/access/mycontent")).toProvider(contribute);
	}

	@Override
	protected NodeProvider getRootNode()
	{
		return node(MyContentSelectSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return null;
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(MyContentSelectResults.class);
	}

	@Override
	protected void addActions(NodeProvider node)
	{
		node.child(SelectionSummarySection.class);
	}

	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(SortOptionsSection.class);
		node.child(FilterByKeywordSection.class);
		node.child(FilterByDateRangeSection.class);
		node.child(FilterByMimeTypeSection.class);
	}

	@Override
	protected String getTreeName()
	{
		return "/access/mycontentselect";
	}
}
