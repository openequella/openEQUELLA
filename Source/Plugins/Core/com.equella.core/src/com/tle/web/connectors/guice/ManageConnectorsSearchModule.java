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

package com.tle.web.connectors.guice;

import com.tle.web.connectors.manage.ConnectorBulkSelectionSection;
import com.tle.web.connectors.manage.ConnectorManagementFavouriteSearchAction;
import com.tle.web.connectors.manage.ConnectorManagementQuerySection;
import com.tle.web.connectors.manage.ConnectorManagementResultsSection;
import com.tle.web.connectors.manage.ConnectorSortOptionsSection;
import com.tle.web.connectors.manage.FilterByArchivedSection;
import com.tle.web.connectors.manage.FilterByCourseSection;
import com.tle.web.connectors.manage.RootConnectorManagementSection;
import com.tle.web.search.guice.AbstractSearchModule;

@SuppressWarnings("nls")
public class ManageConnectorsSearchModule extends AbstractSearchModule
{

	@Override
	protected NodeProvider getRootNode()
	{
		return node(RootConnectorManagementSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(ConnectorManagementQuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(ConnectorManagementResultsSection.class);
	}

	@Override
	protected void addActions(NodeProvider node)
	{
		node.child(ConnectorBulkSelectionSection.class);
	}

	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(ConnectorSortOptionsSection.class);
		node.child(FilterByArchivedSection.class);
		node.child(FilterByCourseSection.class);
	}

	@Override
	protected void addQueryActions(NodeProvider node)
	{
		node.child(ConnectorManagementFavouriteSearchAction.class);
	}

	@Override
	protected String getTreeName()
	{
		return "/access/manageconnectors";
	}
}
