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

import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.item.event.listener.ItemWentLiveListener;

/**
 * @author Andrew Gibb
 */
public class ItemWentLiveEvent extends ApplicationEvent<ItemWentLiveListener>
{
	private static final long serialVersionUID = 1L;
	private final ItemId itemKey;

	public ItemWentLiveEvent(ItemKey key)
	{
		super(PostTo.POST_ONLY_TO_SELF);
		this.itemKey = ItemId.fromKey(key);
	}

	public ItemId getItemId()
	{
		return itemKey;
	}

	@Override
	public Class<ItemWentLiveListener> getListener()
	{
		return ItemWentLiveListener.class;
	}

	@Override
	public void postEvent(ItemWentLiveListener listener)
	{
		listener.itemWentLiveEvent(this);
	}
}
