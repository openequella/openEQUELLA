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
import com.tle.core.item.event.listener.UpdateReferencedUrlsListener;

/**
 * @author Nicholas Read
 */
public class UpdateReferencedUrlsEvent extends ApplicationEvent<UpdateReferencedUrlsListener>
{
	private static final long serialVersionUID = 1L;
	private ItemId itemKey;

	public UpdateReferencedUrlsEvent(ItemKey itemKey)
	{
		super(PostTo.POST_ONLY_TO_SELF);
		this.itemKey = ItemId.fromKey(itemKey);
	}

	public ItemId getItemKey()
	{
		return itemKey;
	}

	@Override
	public Class<UpdateReferencedUrlsListener> getListener()
	{
		return UpdateReferencedUrlsListener.class;
	}

	@Override
	public void postEvent(UpdateReferencedUrlsListener listener)
	{
		listener.updateReferencedUrlsEvent(this);
	}
}
