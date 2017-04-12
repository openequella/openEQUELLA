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
