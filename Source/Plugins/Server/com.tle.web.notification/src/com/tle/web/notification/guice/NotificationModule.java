package com.tle.web.notification.guice;

import com.tle.web.notification.filters.FilterByNotificationReason;
import com.tle.web.notification.section.NotificationResultsSection;
import com.tle.web.notification.section.NotificationSelectionSection;
import com.tle.web.notification.section.RootNotificationListSection;
import com.tle.web.notification.section.RssFeedSection;
import com.tle.web.search.filter.FilterByCollectionSection;
import com.tle.web.search.filter.FilterByDateRangeSection;
import com.tle.web.search.filter.FilterByMimeTypeSection;
import com.tle.web.search.filter.FilterByOwnerSection;
import com.tle.web.search.filter.SimpleResetFiltersQuerySection;
import com.tle.web.search.guice.AbstractSearchModule;

@SuppressWarnings("nls")
public class NotificationModule extends AbstractSearchModule
{
	@Override
	protected NodeProvider getRootNode()
	{
		return node(RootNotificationListSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(SimpleResetFiltersQuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(NotificationResultsSection.class);
	}

	@Override
	protected void addActions(NodeProvider node)
	{
		node.child(NotificationSelectionSection.class);
	}

	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(RssFeedSection.class);
		node.child(FilterByNotificationReason.class);
		node.child(FilterByCollectionSection.class);
		node.child(FilterByOwnerSection.class);
		node.child(FilterByDateRangeSection.class);
		node.child(FilterByMimeTypeSection.class);
	}

	@Override
	protected String getTreeName()
	{
		return "/access/notifications";
	}
}
