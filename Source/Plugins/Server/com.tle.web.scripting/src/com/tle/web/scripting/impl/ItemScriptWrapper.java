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

package com.tle.web.scripting.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.Constants;
import com.dytech.edge.common.PropBagWrapper;
import com.dytech.edge.exceptions.ItemNotFoundException;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ItemXml;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.scripting.objects.ItemScriptObject;
import com.tle.common.scripting.types.CollectionScriptType;
import com.tle.common.scripting.types.ItemScriptType;
import com.tle.core.guice.Bind;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.helper.ItemHelper.ItemHelperSettings;
import com.tle.core.item.service.ItemService;
import com.tle.web.scripting.impl.UtilsScriptWrapper.CollectionScriptTypeImpl;

/**
 * @author aholland
 */
@Bind
@Singleton
public class ItemScriptWrapper extends AbstractScriptWrapper implements ItemScriptObject
{
	private static final long serialVersionUID = 1L;

	@Inject
	private ItemService itemService;
	@Inject
	private ItemHelper itemHelper;

	@Override
	public ItemScriptType getItem(String uuid, int version)
	{
		try
		{
			Item item = itemService.get(new ItemId(uuid, version));
			return new ItemScriptTypeImpl(itemService, itemHelper, item);
		}
		catch( ItemNotFoundException e )
		{
			return null;
		}
	}

	@Override
	public ItemScriptType getLatestVersionItem(String uuid)
	{
		try
		{
			int version = itemService.getLatestVersion(uuid);
			Item item = itemService.get(new ItemId(uuid, version));
			return new ItemScriptTypeImpl(itemService, itemHelper, item);
		}
		catch( ItemNotFoundException e )
		{
			return null;
		}
	}

	@Override
	public ItemScriptType getLiveItem(String uuid)
	{
		try
		{
			int version = itemService.getLiveItemVersion(uuid);
			Item item = itemService.get(new ItemId(uuid, version));
			return new ItemScriptTypeImpl(itemService, itemHelper, item);
		}
		catch( ItemNotFoundException e )
		{
			return null;
		}
	}

	public static class ItemScriptTypeImpl implements ItemScriptType
	{
		private static final long serialVersionUID = 1L;

		private final ItemId itemId;
		private final ItemService service;
		private final ItemHelper itemHelper;

		// lazy
		protected Item item;
		protected PropBagWrapper xml;

		public ItemScriptTypeImpl(ItemService itemService, ItemHelper itemHelper, Item item)
		{
			this(itemService, itemHelper, item.getItemId());
			this.item = item;
		}

		public ItemScriptTypeImpl(ItemService itemService, ItemHelper itemHelper, ItemId itemId)
		{
			this.service = itemService;
			this.itemHelper = itemHelper;
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
						itemHelper.convertToXml(service.getItemPack(itemId), new ItemHelperSettings(true)));
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
}
