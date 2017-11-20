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

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.common.Check;

@SuppressWarnings("nls")
@XmlRootElement
public class SelectedResourceKey implements Serializable
{
	private static final long serialVersionUID = 1L;

	private transient ItemKey itemKey;

	private String uuid;
	private int version;
	private char type;
	private String attachmentUuid = "";
	private String url = "";
	private String folderId;
	// E.g. "cloud"
	private String extensionType;

	public SelectedResourceKey()
	{
		// nothing
	}

	public SelectedResourceKey(ItemKey itemId, String extensionType)
	{
		this(itemId, 'p', null, null, extensionType);
	}

	public SelectedResourceKey(ItemKey itemId, TargetFolder folder, String extensionType)
	{
		this(itemId, 'p', null, folder, extensionType);
	}

	public SelectedResourceKey(ItemKey itemId, String attachmentUuid, String extensionType)
	{
		this(itemId, 'a', attachmentUuid, null, extensionType);
	}

	public SelectedResourceKey(ItemKey itemId, String attachmentUuid, TargetFolder folder, String extensionType)
	{
		this(itemId, 'a', attachmentUuid, folder, extensionType);
	}

	private SelectedResourceKey(ItemKey itemId, char type, String attachmentUuid, TargetFolder folder,
		String extensionType)
	{
		this.itemKey = itemId;
		this.uuid = itemId.getUuid();
		this.version = itemId.getVersion();
		this.type = type;
		this.folderId = (folder == null ? null : folder.getId());
		this.extensionType = extensionType;
		setAttachmentUuid(attachmentUuid);
	}

	@JsonIgnore
	@XmlTransient
	public ItemKey getItemKey()
	{
		if( itemKey == null )
		{
			itemKey = new ItemId(uuid, version);
		}
		return itemKey;
	}

	@XmlElement
	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	@XmlElement
	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	@XmlElement
	public char getType()
	{
		return type;
	}

	public void setType(char type)
	{
		this.type = type;
	}

	@XmlElement
	public String getAttachmentUuid()
	{
		return attachmentUuid;
	}

	public void setAttachmentUuid(String attachmentUuid)
	{
		this.attachmentUuid = Check.nullToEmpty(attachmentUuid);
	}

	@XmlElement
	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = Check.nullToEmpty(url);
	}

	@XmlElement
	public String getFolderId()
	{
		return folderId;
	}

	public void setFolderId(String folderId)
	{
		this.folderId = folderId;
	}

	@XmlElement
	public String getExtensionType()
	{
		return extensionType;
	}

	public void setExtensionType(String extensionType)
	{
		this.extensionType = extensionType;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attachmentUuid == null) ? 0 : attachmentUuid.hashCode());
		result = prime * result + type;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		result = prime * result + version;
		result = prime * result + ((folderId == null) ? 0 : folderId.hashCode());
		result = prime * result + ((extensionType == null) ? 0 : extensionType.hashCode());
		return result;
	}

	public boolean equalsExceptFolder(SelectedResourceKey other)
	{
		if( !Objects.equals(attachmentUuid, other.attachmentUuid) )
		{
			return false;
		}
		if( type != other.type )
		{
			return false;
		}
		if( !Objects.equals(url, other.url) )
		{
			return false;
		}
		if( !Objects.equals(uuid, other.uuid) )
		{
			return false;
		}
		if( !Objects.equals(extensionType, other.extensionType) )
		{
			return false;
		}

		return true;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}
		if( obj == null )
		{
			return false;
		}
		if( getClass() != obj.getClass() )
		{
			return false;
		}
		SelectedResourceKey other = (SelectedResourceKey) obj;
		if( !equalsExceptFolder(other) )
		{
			return false;
		}
		if( !Objects.equals(folderId, other.folderId) )
		{
			return false;
		}
		if( version != other.version )
		{
			return false;
		}
		return true;
	}
}
