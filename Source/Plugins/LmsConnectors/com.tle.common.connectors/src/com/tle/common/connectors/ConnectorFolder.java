package com.tle.common.connectors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Renamed from ConnectorSection to reduce confusion over other Sections :)
public class ConnectorFolder implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String id;
	private final ConnectorCourse course;
	private String name;
	private boolean leaf;
	private Date createdDate;
	private Date modifiedDate;
	private List<ConnectorFolder> folders = new ArrayList<ConnectorFolder>();
	private List<ConnectorContent> content = new ArrayList<ConnectorContent>();
	private boolean available;

	public ConnectorFolder(String id, ConnectorCourse course)
	{
		this.id = id;
		this.course = course;
	}

	public ConnectorCourse getCourse()
	{
		return course;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Date getCreatedDate()
	{
		return createdDate;
	}

	public void setCreatedDate(Date createdDate)
	{
		this.createdDate = createdDate;
	}

	public Date getModifiedDate()
	{
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate)
	{
		this.modifiedDate = modifiedDate;
	}

	public boolean isLeaf()
	{
		return leaf;
	}

	public void setLeaf(boolean leaf)
	{
		this.leaf = leaf;
	}

	public List<ConnectorFolder> getFolders()
	{
		return folders;
	}

	public void setFolders(List<ConnectorFolder> folders)
	{
		this.folders = folders;
	}

	public void addFolder(ConnectorFolder folder)
	{
		folders.add(folder);
	}

	public void addContent(ConnectorContent content)
	{
		this.content.add(content);
	}

	public List<ConnectorContent> getContent()
	{
		return content;
	}

	public void setContent(List<ConnectorContent> content)
	{
		this.content = content;
	}

	public boolean isAvailable()
	{
		return available;
	}

	public void setAvailable(boolean available)
	{
		this.available = available;
	}
}