package com.tle.web.workflow.notification;

import com.tle.web.sections.Bookmark;
import com.tle.web.sections.render.Label;

public class ItemNotification
{
	private Label itemName;
	private Bookmark link;

	public Label getItemName()
	{
		return itemName;
	}

	public void setItemName(Label itemName)
	{
		this.itemName = itemName;
	}

	public Bookmark getLink()
	{
		return link;
	}

	public void setLink(Bookmark link)
	{
		this.link = link;
	}

}