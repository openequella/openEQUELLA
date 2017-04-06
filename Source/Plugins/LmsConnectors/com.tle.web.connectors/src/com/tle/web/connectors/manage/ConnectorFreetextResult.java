package com.tle.web.connectors.manage;

import com.tle.beans.item.ItemIdKey;
import com.tle.common.connectors.ConnectorContent;
import com.tle.core.services.item.FreetextResult;

public class ConnectorFreetextResult extends FreetextResult
{
	private static final long serialVersionUID = 1L;
	private ConnectorContent content;

	public ConnectorFreetextResult(ItemIdKey key, float relevance, boolean sortByRelevance)
	{
		super(key, relevance, sortByRelevance);
	}

	public ConnectorContent getContent()
	{
		return content;
	}

	public void setContent(ConnectorContent content)
	{
		this.content = content;
	}

}
