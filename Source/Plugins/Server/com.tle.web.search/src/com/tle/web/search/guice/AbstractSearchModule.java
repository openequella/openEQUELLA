package com.tle.web.search.guice;

import com.tle.web.search.actions.SearchActionsSection;
import com.tle.web.sections.equella.guice.SectionsModule;
import com.tle.web.sections.equella.search.PagingSection;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;

@SuppressWarnings("nls")
public abstract class AbstractSearchModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		// Root
		NodeProvider root = getRootNode();

		// Query
		NodeProvider queryNode = getQueryNode();
		if( queryNode != null )
		{
			addQueryActions(queryNode);
			root.child(queryNode);
		}

		// Results
		NodeProvider sr = getResultsNode();

		// Result Actions
		NodeProvider resultsActionsNode = getResultActionsNode();
		if( resultsActionsNode != null )
		{
			addSearchResultsActions(resultsActionsNode);
			sr.child(resultsActionsNode);
			root.child(sr);
		}

		// Right Hand Actions
		NodeProvider actionsNode = getActionsNode();
		if( actionsNode != null )
		{
			addActions(actionsNode);
			root.child(actionsNode);
		}

		// Paging
		root.child(getPagingNode());

		// Tree name
		doBinding(root);
	}

	protected abstract NodeProvider getRootNode();

	protected abstract NodeProvider getQueryNode();

	protected abstract NodeProvider getResultsNode();

	protected abstract String getTreeName();

	protected NodeProvider getResultActionsNode()
	{
		return node(SearchResultsActionsSection.class).placeHolder("SEARCH_RESULTS_ACTIONS");
	}

	protected NodeProvider getActionsNode()
	{
		return node(SearchActionsSection.class).placeHolder("SEARCH_ACTIONS");
	}

	protected NodeProvider getPagingNode()
	{
		return node(PagingSection.class);
	}

	protected void addQueryActions(NodeProvider node)
	{
		// To Override
	}

	protected void addSearchResultsActions(NodeProvider node)
	{
		// To Override
	}

	protected void addActions(NodeProvider node)
	{
		// To Override
	}

	protected void doBinding(NodeProvider node)
	{
		bindNamed(getTreeName(), node);
	}
}
