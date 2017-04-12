package com.tle.web.customlinks;

import com.tle.web.sections.standard.model.HtmlLinkState;

public class CustomLinkListComponent
{
	private String name;
	private String uuid;
	private String iconUrl;
	private HtmlLinkState edit;
	private HtmlLinkState delete;

	public CustomLinkListComponent(String name, String uuid, HtmlLinkState edit, HtmlLinkState delete, String iconUrl)
	{
		this.name = name;
		this.uuid = uuid;
		this.edit = edit;
		this.delete = delete;
		this.iconUrl = iconUrl;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public HtmlLinkState getEdit()
	{
		return edit;
	}

	public void setEdit(HtmlLinkState edit)
	{
		this.edit = edit;
	}

	public HtmlLinkState getDelete()
	{
		return delete;
	}

	public void setDelete(HtmlLinkState delete)
	{
		this.delete = delete;
	}

	public void setIconUrl(String iconUrl)
	{
		this.iconUrl = iconUrl;
	}

	public String getIconUrl()
	{
		return iconUrl;
	}
}