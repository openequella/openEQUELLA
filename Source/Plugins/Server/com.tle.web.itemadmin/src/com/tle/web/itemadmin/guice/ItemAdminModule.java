package com.tle.web.itemadmin.guice;

import com.tle.web.itemadmin.section.ItemAdminFavouriteSearchAction;
import com.tle.web.itemadmin.section.ItemAdminFilterByItemStatusSection;
import com.tle.web.itemadmin.section.ItemAdminQuerySection;
import com.tle.web.itemadmin.section.ItemAdminSearchResultsSection;
import com.tle.web.itemadmin.section.ItemAdminSelectionSection;
import com.tle.web.itemadmin.section.ItemAdminWhereSection;
import com.tle.web.itemadmin.section.RootItemAdminSection;
import com.tle.web.search.actions.StandardShareSearchQuerySection;
import com.tle.web.search.filter.FilterByDateRangeSection;
import com.tle.web.search.filter.FilterByMimeTypeSection;
import com.tle.web.search.filter.FilterByOwnerSection;
import com.tle.web.search.guice.AbstractSearchModule;
import com.tle.web.search.sort.SortOptionsSection;
import com.tle.web.sections.Section;
import com.tle.web.sections.equella.search.PagingSection;

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
