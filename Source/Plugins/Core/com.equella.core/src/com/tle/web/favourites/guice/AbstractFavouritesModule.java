/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
