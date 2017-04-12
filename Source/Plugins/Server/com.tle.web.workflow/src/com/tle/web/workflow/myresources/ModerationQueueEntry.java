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
