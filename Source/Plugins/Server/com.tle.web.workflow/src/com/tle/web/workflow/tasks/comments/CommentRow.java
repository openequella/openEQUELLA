/*
 * Created on Sep 2, 2005
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
