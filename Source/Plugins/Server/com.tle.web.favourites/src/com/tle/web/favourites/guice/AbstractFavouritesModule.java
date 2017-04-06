package com.tle.web.favourites.guice;

import com.google.inject.name.Names;
import com.tle.web.search.actions.SearchActionsSection;
import com.tle.web.search.guice.AbstractSearchModule;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.search.PagingSection;

public abstract class AbstractFavouritesModule extends AbstractSearchModule
{
	@Override
	protected void configure()
	{
		bind(SectionTree.class).annotatedWith(Names.named(getTreeName())).toProvider(tree(buildTree(getPrefix())));
	}

	protected abstract String getPrefix();

	@Override
	protected NodeProvider getActionsNode()
	{
		return node(SearchActionsSection.class);
	}

	protected NodeProvider buildTree(String type)
	{
		NodeProvider node = node(null, type);
		NodeProvider actionsNode = getActionsNode();
		if( actionsNode != null )
		{
			node.child(actionsNode);
		}

		NodeProvider searchResults = getResultsNode();
		NodeProvider searchResultsActions = getResultActionsNode();
		addSearchResultsActions(searchResultsActions);
		searchResults.child(searchResultsActions);
		searchResults.child(PagingSection.class);
		node.child(searchResults);

		return node;
	}

	@Override
	protected NodeProvider getRootNode()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		throw new UnsupportedOperationException();
	}
}
