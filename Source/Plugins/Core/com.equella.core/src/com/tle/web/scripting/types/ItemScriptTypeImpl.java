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

package com.tle.web.scripting.types;

import com.dytech.edge.common.Constants;
import com.dytech.edge.common.PropBagWrapper;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ItemXml;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.scripting.types.CollectionScriptType;
import com.tle.common.scripting.types.ItemScriptType;
import com.tle.common.security.SecurityConstants;
import com.tle.core.item.ViewCountJavaDao;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.service.ItemService;
import com.tle.core.security.TLEAclManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ItemScriptTypeImpl implements ItemScriptType
{
	private static final long serialVersionUID = 1L;

	@Inject
	private ItemService service;
	@Inject
	private TLEAclManager aclService;
	@Inject
	private ItemHelper itemHelper;

	private final ItemId itemId;

	// lazy
	protected Item item;
	protected PropBagWrapper xml;
	protected Integer viewCount;
	// because viewCount can legitimately be null,
	// we need to set this to stop repeatedly trying to calculate it
	protected boolean viewCountRetrieved;

	@AssistedInject
	protected ItemScriptTypeImpl(@Assisted("item") Item item)
	{
		this(item.getItemId());
		this.item = item;
	}

	@AssistedInject
	protected ItemScriptTypeImpl(@Assisted("itemId") ItemId itemId)
	{
		this.itemId = itemId;
		this.item = null;
		this.xml = null;
	}

	@Override
	public String getDescription()
	{
		return CurrentLocale.get(getItem().getDescription(), Constants.BLANK);
	}

	@Override
	public String getName()
	{
		return CurrentLocale.get(getItem().getName(), getUuid());
	}

	@Override
	public String getUuid()
	{
		return itemId.getUuid();
	}

	@Override
	public int getVersion()
	{
		return itemId.getVersion();
	}

	@Override
	public PropBagWrapper getXml()
	{
		if( xml == null )
		{
			ItemXml itemXml = getItem().getItemXml();
			if( itemXml != null )
			{
				xml = new PropBagWrapper(
						itemHelper.convertToXml(service.getItemPack(itemId), new ItemHelper.ItemHelperSettings(true)));
			}
		}
		return xml;
	}

	@Override
	public String getItemStatus()
	{
		ItemStatus status = getItem().getStatus();
		if( status == null )
		{
			return null;
		}
		return status.toString();
	}

	@Override
	public String getOwner()
	{
		return getItem().getOwner();
	}

	@Override
	public void setOwner(String userUniqueId)
	{
		getItem().setOwner(userUniqueId);
	}

	@Override
	public CollectionScriptType getCollection()
	{
		return new CollectionScriptTypeImpl(getItem().getItemDefinition());
	}

	@Override
	public void addSharedOwner(String userUniqueId)
	{
		getItem().getCollaborators().add(userUniqueId);
	}

	@Override
	public boolean removeSharedOwner(String userUniqueId)
	{
		return getItem().getCollaborators().remove(userUniqueId);
	}

	@Override
	public List<String> listSharedOwners()
	{
		// return a COPY of the list
		return new ArrayList<String>(getItem().getCollaborators());
	}

	@Override
	public void setThumbnail(String thumbnail)
	{
		getItem().setThumb(thumbnail);
	}

	@Override
	public String getThumbnail()
	{
		return getItem().getThumb();
	}

	@Override
	public Integer getViewCount()
	{
		if (viewCountRetrieved)
		{
			return viewCount;
		}
		if (!aclService.filterNonGrantedPrivileges(item, SecurityConstants.VIEW_VIEWCOUNT).isEmpty())
		{
			viewCount = ViewCountJavaDao.getSummaryViewCount(itemId);
		}
		viewCountRetrieved = true;
		return viewCount;
	}

	/**
	 * Internal use only! Do NOT use in scripts
	 *
	 * @return
	 */
	public Item getItem()
	{
		if( item == null )
		{
			item = service.get(itemId);
		}
		return item;
	}
}
