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

import java.util.EventListener;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
public class ChangePageEvent extends AbstractMyPagesEvent<ChangePageEventListener>
{
	private final String oldPageUuid;
	private final String newPageUuid;

	public ChangePageEvent(String oldPageUuid, String newPageUuid, String sessionId)
	{
		super(sessionId);
		this.oldPageUuid = oldPageUuid;
		this.newPageUuid = newPageUuid;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, ChangePageEventListener listener) throws Exception
	{
		listener.changePage(info, this);
	}

	@Override
	public Class<? extends EventListener> getListenerClass()
	{
		return ChangePageEventListener.class;
	}

	public String getOldPageUuid()
	{
		return oldPageUuid;
	}

	public String getNewPageUuid()
	{
		return newPageUuid;
	}
}
