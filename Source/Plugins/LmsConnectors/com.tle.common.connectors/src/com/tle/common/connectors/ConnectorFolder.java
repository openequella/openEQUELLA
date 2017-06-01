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