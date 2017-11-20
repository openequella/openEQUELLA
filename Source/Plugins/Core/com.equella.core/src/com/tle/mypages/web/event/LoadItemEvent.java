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

package com.tle.mypages.web.event;

import com.tle.beans.item.Item;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
public class LoadItemEvent extends AbstractMyPagesEvent<LoadItemEventListener>
{
	private final Item item;
	private final String pageUuid;

	public LoadItemEvent(String sessionId, Item item, String pageUuid)
	{
		super(sessionId);
		this.item = item;
		this.pageUuid = pageUuid;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, LoadItemEventListener listener) throws Exception
	{
		listener.doLoadItemEvent(info, this);
	}

	@Override
	public Class<LoadItemEventListener> getListenerClass()
	{
		return LoadItemEventListener.class;
	}

	public Item getItem()
	{
		return item;
	}

	public String getPageUuid()
	{
		return pageUuid;
	}
}
