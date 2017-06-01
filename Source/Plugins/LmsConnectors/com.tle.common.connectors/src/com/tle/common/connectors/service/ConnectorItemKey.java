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

package com.tle.common.connectors.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tle.beans.item.AbstractItemKey;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.common.connectors.ConnectorContent;

public class ConnectorItemKey extends AbstractItemKey
{
	private static final long serialVersionUID = 1L;
	private static final Pattern EXTRACT_CONTENT_ID = Pattern.compile("^([^\\./]+)\\.([^/]+)\\.([^/]+)/(\\d+)/(.*)$"); //$NON-NLS-1$
	private String contentId;
	private long connectorId;
	private String title;

	@SuppressWarnings("nls")
	public ConnectorItemKey(String fullid)
	{
		Matcher m = EXTRACT_CONTENT_ID.matcher(fullid);
		if( m.matches() )
		{
			uuid = m.group(1);
			contentId = m.group(2);
			connectorId = Long.parseLong(m.group(3));
			version = Integer.parseInt(m.group(4));
			title = m.group(5);
		}
		else
		{
			throw new IllegalArgumentException("String isn't an ConnectorItemKey:" + fullid);
		}
	}

	public ConnectorItemKey(ConnectorContent content, long connectorId)
	{
		super(content.getUuid(), content.getVersion());
		this.contentId = content.getId();
		this.connectorId = connectorId;
		this.title = content.getExternalTitle();
	}

	@Override
	public String toString(int version)
	{
		return getUuid() + '.' + contentId + '.' + connectorId + '/' + version + '/' + title;
	}

	public String toKeyString()
	{
		return getUuid() + '.' + contentId + '.' + connectorId + '.' + '/' + version;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() + contentId.hashCode() + (int) connectorId;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( !super.equals(obj) )
		{
			return false;
		}
		return (contentId.equals(((ConnectorItemKey) obj).contentId)
			&& connectorId == ((ConnectorItemKey) obj).connectorId);
	}

	public static ItemKey parse(String fullid)
	{
		Matcher m = EXTRACT_CONTENT_ID.matcher(fullid);
		if( m.matches() )
		{
			return new ConnectorItemKey(fullid);
		}
		return new ItemId(fullid);
	}

	public String getContentId()
	{
		return contentId;
	}

	public long getConnectorId()
	{
		return connectorId;
	}

	public void setConnectorId(long connectorId)
	{
		this.connectorId = connectorId;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}
}
