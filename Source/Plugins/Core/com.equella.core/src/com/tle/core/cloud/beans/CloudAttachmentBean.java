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

package com.tle.core.cloud.beans;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.google.common.collect.Maps;

/**
 * @author Aaron
 */
@XmlRootElement
public class CloudAttachmentBean
{
	private String type;
	private String uuid;
	private String description;
	private boolean preview;
	private String filename;
	private String md5;
	private String attachmentType;
	private String parentZip;
	private boolean external;

	private final Map<String, Object> extras = Maps.newHashMap();

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public boolean isPreview()
	{
		return preview;
	}

	public void setPreview(boolean preview)
	{
		this.preview = preview;
	}

	public Object get(String name)
	{
		return extras.get(name);
	}

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public String getMd5()
	{
		return md5;
	}

	public void setMd5(String md5)
	{
		this.md5 = md5;
	}

	public String getAttachmentType()
	{
		return attachmentType;
	}

	public void setAttachmentType(String attachmentType)
	{
		this.attachmentType = attachmentType;
	}

	public boolean isExternal()
	{
		return external;
	}

	public void setExternal(boolean external)
	{
		this.external = external;
	}

	public String getParentZip()
	{
		return parentZip;
	}

	public void setParentZip(String parentZip)
	{
		this.parentZip = parentZip;
	}

	@JsonAnyGetter
	public Map<String, Object> any()
	{
		return extras;
	}

	@JsonAnySetter
	public void set(String key, Object value)
	{
		extras.put(key, value);
	}
}
