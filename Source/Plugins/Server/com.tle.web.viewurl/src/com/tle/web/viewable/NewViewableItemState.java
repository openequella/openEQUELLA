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

package com.tle.web.viewable;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.common.base.Throwables;
import com.tle.beans.item.ItemKey;
import com.tle.core.services.UrlService;

public class NewViewableItemState implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String context = "items/"; //$NON-NLS-1$
	private boolean itemForReal = true;
	private boolean requireDRM = true;
	private boolean latest;
	private ItemKey itemId;
	private String itemdir;
	private String integrationType;

	public String getContext()
	{
		return context;
	}

	public void setContext(String context)
	{
		this.context = context;
		itemdir = null;
	}

	public boolean isItemForReal()
	{
		return itemForReal;
	}

	public void setItemForReal(boolean itemForReal)
	{
		this.itemForReal = itemForReal;
	}

	public boolean isRequireDRM()
	{
		return requireDRM;
	}

	public void setRequireDRM(boolean requireDRM)
	{
		this.requireDRM = requireDRM;
	}

	public ItemKey getItemId()
	{
		return itemId;
	}

	public void setItemId(ItemKey itemId)
	{
		if( itemId == null )
		{
			throw new NullPointerException("itemId cannot be null");
		}
		this.itemId = itemId;
		itemdir = null;
	}

	public String getItemdir(UrlService urlService)
	{
		if( itemdir == null )
		{
			String firstPart = urlService.getInstitutionUrl().getFile();
			String itemIdPart;
			if( latest )
			{
				itemIdPart = itemId.toString(0);
			}
			else
			{
				itemIdPart = itemId.toString();
			}
			itemdir = firstPart + context + itemIdPart + '/';
		}
		return itemdir;
	}

	public URI getServletPath()
	{
		String itemIdPart;
		if( latest )
		{
			itemIdPart = itemId.toString(0);
		}
		else
		{
			itemIdPart = itemId.toString();
		}
		try
		{
			return new URI(null, null, '/' + context + itemIdPart + '/', null);
		}
		catch( URISyntaxException e )
		{
			throw Throwables.propagate(e);
		}
	}

	public String getIntegrationType()
	{
		return integrationType;
	}

	public void setIntegrationType(String integrationType)
	{
		this.integrationType = integrationType;
	}

	public boolean isLatest()
	{
		return latest;
	}

	public void setLatest(boolean latest)
	{
		this.latest = latest;
	}
}
