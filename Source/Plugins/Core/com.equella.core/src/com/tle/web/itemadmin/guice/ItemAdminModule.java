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

package com.tle.web.itemadmin.guice;

import com.tle.web.itemadmin.section.ItemAdminFavouriteSearchAction;
import com.tle.web.itemadmin.section.ItemAdminFilterByItemStatusSection;
import com.tle.web.itemadmin.section.ItemAdminQuerySection;
import com.tle.web.itemadmin.section.ItemAdminSearchResultsSection;
import com.tle.web.itemadmin.section.ItemAdminSelectionSection;
import com.tle.web.itemadmin.section.ItemAdminWhereSection;
import com.tle.web.itemadmin.section.RootItemAdminSection;
import com.tle.web.search.actions.StandardShareSearchQuerySection;
import com.tle.web.search.filter.FilterByBadUrlSection;
import com.tle.web.search.filter.FilterByDateRangeSection;
import com.tle.web.search.filter.FilterByMimeTypeSection;
import com.tle.web.search.filter.FilterByOwnerSection;
import com.tle.web.search.guice.AbstractSearchModule;
import com.tle.web.search.sort.SortOptionsSection;
import com.tle.web.sections.Section;
import com.tle.web.sections.equella.search.PagingSection;
import com.tle.web.workflow.manage.FilterByWorkflowSection;
import com.tle.web.workflow.manage.FilterByWorkflowTaskSection;

@SuppressWarnings("nls")
public class ItemAdminModule extends AbstractSearchModule
{
	@Override
	public NodeProvider getRootNode()
	{
		NodeProvider node = node(RootItemAdminSection.class);
		node.innerChild(ItemAdminWhereSection.class);
		return node;
	}

	@Override
	public NodeProvider getQueryNode()
	{
		return node(ItemAdminQuerySection.class);
	}

	@Override
	public NodeProvider getResultsNode()
	{
		return node(ItemAdminSearchResultsSection.class);
	}

	@Override
	protected void addQueryActions(NodeProvider node)
	{
		node.child(ItemAdminFavouriteSearchAction.class);
	}

	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(SortOptionsSection.class);
		node.child(new NodeProvider(FilterByOwnerSection.class)
		{
			@Override
			protected void customize(Section section)
			{
				FilterByOwnerSection fbos = (FilterByOwnerSection) section;
				fbos.setShowOrphaned(true);
			}
		});
		node.child(FilterByDateRangeSection.class);
		node.child(ItemAdminFilterByItemStatusSection.class);
		node.child(FilterByMimeTypeSection.class);
		node.child(StandardShareSearchQuerySection.class);
		node.child(FilterByBadUrlSection.class);
		node.child(FilterByWorkflowSection.class);
		node.child(FilterByWorkflowTaskSection.class);
	}

	@Override
	protected void addActions(NodeProvider actions)
	{
		actions.child(ItemAdminSelectionSection.class);
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

	@Override
	protected String getTreeName()
	{
		return "/access/itemadmin";
	}
}
