package com.tle.web.connectors.manage;

import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.service.ConnectorRepositoryService.ExternalContentSortType;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;

public class ConnectorManagementSearchEvent extends AbstractSearchEvent<ConnectorManagementSearchEvent>
{
	private final Connector connector;
	private final ConnectorContentSearch search;

	public ConnectorManagementSearchEvent(ConnectorContentSearch search, Connector connector)
	{
		super(null); // broadcast
		this.search = search;
		this.connector = connector;
	}

	public Connector getConnector()
	{
		return connector;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchEventListener<ConnectorManagementSearchEvent> listener)
		throws Exception
	{
		listener.prepareSearch(info, this);
	}

	public ConnectorContentSearch getSearch()
	{
		return search;
	}

	public void setSort(ExternalContentSortType sort)
	{
		search.setSort(sort);
	}
}
