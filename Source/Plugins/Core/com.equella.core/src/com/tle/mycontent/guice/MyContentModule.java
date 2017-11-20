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
