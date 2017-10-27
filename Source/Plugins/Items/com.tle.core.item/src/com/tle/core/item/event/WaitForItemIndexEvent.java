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
import com.tle.core.item.event.listener.WaitForItemIndexListener;

/**
 * @author Nicholas Read
 */
public class WaitForItemIndexEvent extends ApplicationEvent<WaitForItemIndexListener>
{
	private static final long serialVersionUID = 1L;
	private ItemIdKey itemIdKey;

	public WaitForItemIndexEvent(ItemIdKey itemIdKey)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.itemIdKey = itemIdKey;
	}

	public ItemIdKey getItemIdKey()
	{
		return itemIdKey;
	}

	@Override
	public Class<WaitForItemIndexListener> getListener()
	{
		return WaitForItemIndexListener.class;
	}

	@Override
	public void postEvent(WaitForItemIndexListener listener)
	{
		listener.waitForItem(this);
	}

}
