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

package com.tle.web.workflow.myresources;

import java.util.Date;

import com.tle.core.guice.Bind;
import com.tle.web.itemlist.item.AbstractItemListEntry;
import com.tle.web.sections.standard.model.HtmlLinkState;

@Bind
public class ModerationQueueEntry extends AbstractItemListEntry
{
	private HtmlLinkState rejectMessage;

	public Date getSubmittedDate()
	{
		return getItem().getModeration().getStart();
	}

	public Date getLastActionDate()
	{
		return getItem().getModeration().getLastAction();
	}

	public HtmlLinkState getRejectMessage()
	{
		return rejectMessage;
	}

	public void setRejectMessage(HtmlLinkState rejectMessage)
	{
		this.rejectMessage = rejectMessage;
	}
}
