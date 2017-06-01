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

package com.tle.web.remoterepo.merlot;

import com.tle.web.remoterepo.merlot.filter.MerlotDateRangeFilterSection;
import com.tle.web.remoterepo.merlot.filter.MerlotFilterKeywordTypeSection;
import com.tle.web.remoterepo.merlot.filter.MerlotFilterOptionsSection;
import com.tle.web.remoterepo.merlot.sort.MerlotSortOptionsSection;
import com.tle.web.search.guice.AbstractSearchModule;

@SuppressWarnings("nls")
public class MerlotModule extends AbstractSearchModule
{
	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(MerlotSortOptionsSection.class);
		node.child(MerlotFilterKeywordTypeSection.class);
		node.child(MerlotFilterOptionsSection.class);
		node.child(MerlotDateRangeFilterSection.class);
	}

	@Override
	protected NodeProvider getPagingNode()
	{
		return node(MerlotPagingSection.class);
	}

	@Override
	protected NodeProvider getRootNode()
	{
		return node(MerlotRootRemoteRepoSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(MerlotQuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(MerlotResultsSection.class);
	}

	@Override
	protected String getTreeName()
	{
		return "merlotTree";
	}
}
