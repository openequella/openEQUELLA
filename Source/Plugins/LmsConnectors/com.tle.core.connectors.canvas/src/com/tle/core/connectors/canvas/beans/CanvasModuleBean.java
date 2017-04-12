package com.tle.core.connectors.canvas.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
public class CanvasModuleBean
{
	private String id;
	private String name;
	@JsonProperty(value = "items_count")
	private int itemsCount;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public int getItemsCount()
	{
		return itemsCount;
	}

	public void setItemsCount(int itemsCount)
	{
		this.itemsCount = itemsCount;
	}
}
