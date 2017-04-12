package com.tle.core.connectors.canvas.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
public class CanvasModuleItemBean
{
	private String id;
	private String title;
	@JsonProperty(value = "external_url")
	private String equellaUrl;

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getEquellaUrl()
	{
		return equellaUrl;
	}

	public void setEquellaUrl(String equellaUrl)
	{
		this.equellaUrl = equellaUrl;
	}
}
