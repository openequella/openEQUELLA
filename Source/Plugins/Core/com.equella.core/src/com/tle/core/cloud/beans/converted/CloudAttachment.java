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

package com.tle.core.cloud.beans.converted;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.IAttachment;

public class CloudAttachment implements IAttachment, Serializable
{
	public static final String TYPE_FILE = "file";
	public static final String TYPE_URL = "url";

	private String uuid;
	private String description;
	private String filename;
	private String md5sum;
	private String thumbnail;
	private String viewUrl;
	private String type;
	private Map<String, Object> data;
	private boolean dataModified;

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public String getUrl()
	{
		return filename;
	}

	@Override
	public void setUrl(String url)
	{
		filename = url;
	}

	@Override
	public void setData(String name, Object value)
	{
		if( data == null )
		{
			data = new HashMap<String, Object>();
		}
		else if( !dataModified )
		{
			data = new HashMap<String, Object>(data);
		}
		dataModified = true;
		data.put(name, value);
	}

	@Override
	public Object getData(String name)
	{
		return data == null ? null : data.get(name);
	}

	@Override
	public Map<String, Object> getDataAttributesReadOnly()
	{
		if( data == null )
		{
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(data);
	}

	@Override
	public Map<String, Object> getDataAttributes()
	{
		return data;
	}

	@Override
	public void setDataAttributes(Map<String, Object> data)
	{
		this.data = data;
	}

	@Override
	public AttachmentType getAttachmentType()
	{
		return AttachmentType.OTHER;
	}

	@Override
	public String getUuid()
	{
		return uuid;
	}

	@Override
	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	@Override
	public String getThumbnail()
	{
		return thumbnail;
	}

	@Override
	public void setThumbnail(String thumbnail)
	{
		this.thumbnail = thumbnail;
	}

	@Override
	public String getMd5sum()
	{
		return md5sum;
	}

	@Override
	public void setMd5sum(String md5sum)
	{
		this.md5sum = md5sum;
	}

	@Override
	public String getViewer()
	{
		// Cloud items don't have a configured viewer
		return null;
	}

	@Override
	public void setViewer(String viewer)
	{
		// No
	}

	@Override
	public boolean isPreview()
	{
		return false;
	}

	@Override
	public void setPreview(boolean preview)
	{
		// No
	}

	public String getViewUrl()
	{
		return viewUrl;
	}

	public void setViewUrl(String viewUrl)
	{
		this.viewUrl = viewUrl;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	@Override
	public void setRestricted(boolean restricted)
	{
		// fahget aboutit
	}

	@Override
	public boolean isRestricted()
	{
		return false;
	}
}
