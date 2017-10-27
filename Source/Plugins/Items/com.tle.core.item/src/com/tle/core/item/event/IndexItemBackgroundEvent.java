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

package com.tle.core.item.event;

import com.tle.beans.item.ItemIdKey;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.item.event.listener.IndexItemBackgroundListener;

/**
 * @author Nicholas Read
 */
public class IndexItemBackgroundEvent extends ApplicationEvent<IndexItemBackgroundListener>
{
	private static final long serialVersionUID = 1L;
	private final ItemIdKey itemIdKey;

	public IndexItemBackgroundEvent(ItemIdKey itemId, boolean self)
	{
		super(self ? PostTo.POST_TO_ALL_CLUSTER_NODES : PostTo.POST_TO_OTHER_CLUSTER_NODES);
		this.itemIdKey = itemId;
	}

	public ItemIdKey getItemIdKey()
	{
		return itemIdKey;
	}

	@Override
	public Class<IndexItemBackgroundListener> getListener()
	{
		return IndexItemBackgroundListener.class;
	}

	@Override
	public void postEvent(IndexItemBackgroundListener listener)
	{
		listener.indexItemBackgroundEvent(this);
	}
}
