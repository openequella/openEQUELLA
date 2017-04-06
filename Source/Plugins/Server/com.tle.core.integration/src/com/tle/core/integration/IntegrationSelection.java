package com.tle.core.integration;

import com.tle.beans.item.ItemId;

public class IntegrationSelection
{
	private final ItemId itemId;
	private char type;
	private String selection;
	private String resource;
	private String contentType;
	private boolean latest;

	public IntegrationSelection(ItemId itemId)
	{
		this.itemId = itemId;
	}

	public ItemId getItemId()
	{
		return itemId;
	}

	public char getType()
	{
		return type;
	}

	public void setType(char type)
	{
		this.type = type;
	}

	public String getSelection()
	{
		return selection;
	}

	public void setSelection(String selection)
	{
		this.selection = selection;
	}

	public String getResource()
	{
		return resource;
	}

	public void setResource(String resource)
	{
		this.resource = resource;
	}

	public String getContentType()
	{
		return contentType;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	public boolean isLatest()
	{
		return latest;
	}

	public void setLatest(boolean latest)
	{
		this.latest = latest;
	}
}
