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

package com.tle.web.myresources.guice;

import com.tle.web.myresources.MyResourcesFavouriteSearchAction;
import com.tle.web.myresources.MyResourcesSearchResults;
import com.tle.web.myresources.MyResourcesSearchTypeSection;
import com.tle.web.myresources.MyResourcesSortSection;
import com.tle.web.myresources.RootMyResourcesSection;
import com.tle.web.search.actions.StandardShareSearchQuerySection;
import com.tle.web.search.filter.FilterByAutoCompleteKeywordSection;
import com.tle.web.search.filter.FilterByCollectionSection;
import com.tle.web.search.filter.FilterByDateRangeSection;
import com.tle.web.search.filter.FilterByItemStatusSection;
import com.tle.web.search.filter.FilterByMimeTypeSection;
import com.tle.web.search.guice.AbstractSearchModule;

@SuppressWarnings("nls")
public class MyResourcesModule extends AbstractSearchModule
{
	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(StandardShareSearchQuerySection.class);
		node.child(MyResourcesSortSection.class);
		node.child(FilterByAutoCompleteKeywordSection.class);
		node.child(FilterByCollectionSection.class);
		node.child(FilterByItemStatusSection.class);
		node.child(FilterByDateRangeSection.class);
		node.child(FilterByMimeTypeSection.class);
	}

	@Override
	protected NodeProvider getRootNode()
	{
		return node(RootMyResourcesSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(MyResourcesSearchTypeSection.class);
	}

	@Override
	protected void addQueryActions(NodeProvider node)
	{
		node.child(MyResourcesFavouriteSearchAction.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(MyResourcesSearchResults.class);
	}

	@Override
	protected String getTreeName()
	{
		return "/access/myresources";
	}
}
