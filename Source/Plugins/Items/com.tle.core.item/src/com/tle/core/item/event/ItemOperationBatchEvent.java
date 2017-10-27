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

import java.util.ArrayList;
import java.util.List;

import com.tle.core.events.ApplicationEvent;
import com.tle.core.item.event.listener.ItemOperationBatchListener;

/**
 * @author Nicholas Read
 */
public class ItemOperationBatchEvent extends ApplicationEvent<ItemOperationBatchListener>
{
	private final List<ItemOperationEvent> events;

	public ItemOperationBatchEvent()
	{
		super(PostTo.POST_ONLY_TO_SELF);
		events = new ArrayList<ItemOperationEvent>();
	}

	public ItemOperationBatchEvent addEvent(ItemOperationEvent event)
	{
		events.add(event);
		return this;
	}

	public List<ItemOperationEvent> getEvents()
	{
		return events;
	}

	@Override
	public Class<ItemOperationBatchListener> getListener()
	{
		return ItemOperationBatchListener.class;
	}

	@Override
	public void postEvent(ItemOperationBatchListener listener)
	{
		listener.itemOperationBatchEvent(this);
	}
}
