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

package com.tle.web.browseby.guice;

import com.tle.web.browseby.BrowsePage;
import com.tle.web.browseby.BrowseSearchResults;
import com.tle.web.browseby.BrowseSection;
import com.tle.web.search.actions.ShareSearchQueryAction;
import com.tle.web.search.filter.FilterByDateRangeSection;
import com.tle.web.search.filter.FilterByKeywordSection;
import com.tle.web.search.filter.FilterByMimeTypeSection;
import com.tle.web.search.filter.FilterByOwnerSection;
import com.tle.web.search.guice.AbstractSearchModule;
import com.tle.web.search.sort.SortOptionsSection;

@SuppressWarnings("nls")
public class BrowseByModule extends AbstractSearchModule
{
	@Override
	protected String getTreeName()
	{
		return "/access/browseby";
	}

	@Override
	protected NodeProvider getRootNode()
	{
		NodeProvider node = node(BrowsePage.class);
		node.child(BrowseSection.class);
		return node;
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return null;
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(BrowseSearchResults.class);
	}

	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(ShareSearchQueryAction.class);
		node.child(SortOptionsSection.class);
		node.child(FilterByKeywordSection.class);
		node.child(FilterByOwnerSection.class);
		node.child(FilterByDateRangeSection.class);
		node.child(FilterByMimeTypeSection.class);
	}
}
