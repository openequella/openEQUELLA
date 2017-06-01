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

package com.tle.web.workflow.tasks.comments;

import java.util.Date;

import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.standard.model.HtmlLinkState;

public class CommentRow
{
	private final String message;
	private final HtmlLinkState user;
	private final Label taskName;
	private final Date date;
	private final TagRenderer dateRenderer;
	private final String extraClass;

	public CommentRow(String message, HtmlLinkState user, Label taskName, Date date, TagRenderer dateRenderer,
		String extraClass)
	{
		this.message = message;
		this.user = user;
		this.taskName = taskName;
		this.date = date;
		this.dateRenderer = dateRenderer;
		this.extraClass = extraClass;
	}

	public String getMessage()
	{
		return message;
	}

	public HtmlLinkState getUser()
	{
		return user;
	}

	public Label getTaskName()
	{
		return taskName;
	}

	public Date getDate()
	{
		return date;
	}

	public TagRenderer getDateRenderer()
	{
		return dateRenderer;
	}

	public String getExtraClass()
	{
		return extraClass;
	}

}
