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

import com.google.inject.assistedinject.Assisted;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.scripting.types.CollectionScriptType;
import com.tle.core.item.ViewCountJavaDao;

import javax.inject.Inject;

public class CollectionScriptTypeImpl implements CollectionScriptType
{
	private static final long serialVersionUID = 1L;

	private final ItemDefinition collection;

	// lazy
	protected Integer itemViewCount;
	protected Integer attachmentViewCount;
	// because viewCount can legitimately be null,
	// we need to set this to stop repeatedly trying to calculate it
	protected boolean itemViewCountRetrieved;
	protected boolean attachmentViewCountRetrieved;

	@Inject
	protected CollectionScriptTypeImpl(@Assisted("collection") ItemDefinition collection)
	{
		this.collection = collection;
	}

	@Override
	public String getName()
	{
		return CurrentLocale.get(collection.getName(), collection.getUuid());
	}

	@Override
	public String getDescription()
	{
		return CurrentLocale.get(collection.getDescription());
	}

	@Override
	public String getUniqueID()
	{
		return collection.getUuid();
	}

	@Override
	public String getUuid()
	{
		return collection.getUuid();
	}

	@Override
	public Integer getItemViewCount()
	{
		if (itemViewCountRetrieved)
		{
			return itemViewCount;
		}
		itemViewCount = ViewCountJavaDao.getSummaryViewsForCollection(collection);
		itemViewCountRetrieved = true;
		return itemViewCount;
	}

	@Override
	public Integer getAttachmentViewCount()
	{
		if (attachmentViewCountRetrieved)
		{
			return attachmentViewCount;
		}
		attachmentViewCount = ViewCountJavaDao.getAttachmentViewsForCollection(collection);
		attachmentViewCountRetrieved = true;
		return attachmentViewCount;
	}
}
