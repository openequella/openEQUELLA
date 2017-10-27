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

package com.tle.core.freetext.event;

import com.tle.core.events.ApplicationEvent;
import com.tle.core.freetext.event.listener.ItemReindexListener;
import com.tle.core.freetext.reindex.ReindexFilter;

/**
 * @author Nicholas Read
 */
public class ItemReindexEvent extends ApplicationEvent<ItemReindexListener>
{
	private static final long serialVersionUID = 1L;
	private ReindexFilter filter;

	public ItemReindexEvent(ReindexFilter filter)
	{
		super(PostTo.POST_ONLY_TO_SELF);
		this.filter = filter;
	}

	public ReindexFilter getFilter()
	{
		return filter;
	}

	@Override
	public Class<ItemReindexListener> getListener()
	{
		return ItemReindexListener.class;
	}

	@Override
	public void postEvent(ItemReindexListener listener)
	{
		listener.itemReindexEvent(this);
	}
}
