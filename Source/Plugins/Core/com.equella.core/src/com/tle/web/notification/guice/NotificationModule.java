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
