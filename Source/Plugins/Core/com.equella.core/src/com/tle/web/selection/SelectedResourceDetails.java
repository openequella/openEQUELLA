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

package com.tle.web.selection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tle.beans.item.ItemId;

@XmlRootElement
public class SelectedResourceDetails
{
	private SelectedResourceKey key;
	private boolean latest;
	private String title;
	private String description;

	public SelectedResourceDetails()
	{
		key = new SelectedResourceKey();
	}

	public SelectedResourceDetails(SelectedResource resource)
	{
		key = resource.getKey();
		this.latest = resource.isLatest();
		this.title = resource.getTitle();
		this.description = resource.getDescription();
	}

	public SelectedResourceDetails(ItemId itemId, String extensionType)
	{
		key = new SelectedResourceKey(itemId, extensionType);
	}

	public SelectedResourceDetails(ItemId itemId, String attachUuid, String extensionType)
	{
		key = new SelectedResourceKey(itemId, attachUuid, extensionType);
	}

	@XmlElement
	public SelectedResourceKey getKey()
	{
		return key;
	}

	/**
	 * For JSON support
	 */
	public void setKey(SelectedResourceKey key)
	{
		this.key = key;
	}

	@JsonIgnore
	@XmlTransient
	public String getAttachmentUuid()
	{
		return key.getAttachmentUuid();
	}

	@JsonIgnore
	@XmlTransient
	public char getType()
	{
		return key.getType();
	}

	@JsonIgnore
	@XmlTransient
	public String getUrl()
	{
		return key.getUrl();
	}

	@JsonIgnore
	@XmlTransient
	public String getUuid()
	{
		return key.getUuid();
	}

	@JsonIgnore
	@XmlTransient
	public int getVersion()
	{
		return key.getVersion();
	}

	@XmlElement
	public boolean isLatest()
	{
		return latest;
	}

	public void setLatest(boolean latest)
	{
		this.latest = latest;
	}

	@XmlElement
	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	@XmlElement
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public ItemId createItemId()
	{
		return new ItemId(key.getUuid(), key.getVersion());
	}
}
