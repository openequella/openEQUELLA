package com.tle.core.connectors.blackboard.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Content
{
	private String id;
	private String title;
	private String created;
	private Integer position;
	private Boolean hasChildren;
	private Availability availability;
	private ContentHandler contentHandler;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getCreated()
	{
		return created;
	}

	public void setCreated(String created)
	{
		this.created = created;
	}

	public Integer getPosition()
	{
		return position;
	}

	public void setPosition(Integer position)
	{
		this.position = position;
	}

	public Boolean getHasChildren()
	{
		return hasChildren;
	}

	public void setHasChildren(Boolean hasChildren)
	{
		this.hasChildren = hasChildren;
	}

	public Availability getAvailability()
	{
		return availability;
	}

	public void setAvailability(Availability availability)
	{
		this.availability = availability;
	}

	public ContentHandler getContentHandler()
	{
		return contentHandler;
	}

	public void setContentHandler(ContentHandler contentHandler)
	{
		this.contentHandler = contentHandler;
	}

	@XmlRootElement
	public static class ContentHandler
	{
		private String id; // resource/x-bb-folder

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}
	}
}
