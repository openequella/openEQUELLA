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

package com.tle.beans.item.attachments;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.google.common.base.Strings;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.IdCloneable;
import com.tle.beans.item.Item;

@Entity
@AccessType("field")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "type")
public abstract class Attachment implements IAttachment, Serializable, Cloneable, IdCloneable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", insertable = false, updatable = false, nullable = false)
	@XStreamOmitField
	@Index(name = "attachmentItem")
	private Item item;

	@Column(length = 40, nullable = false)
	@Index(name = "attachmentUuidIndex")
	private String uuid;

	@Column(length = 1024)
	private String url;

	@Column(length = 1024)
	protected String description;

	@Column(length = 1024)
	protected String value1;

	@Column(length = 1024)
	protected String value2;

	@Column(length = 1024)
	protected String value3;

	@Column(length = 512)
	protected String thumbnail;

	@Type(type = "xstream_immutable")
	@Column(length = 8192)
	private Map<String, Object> data;
	private transient boolean dataModified;

	@Column(length = 32)
	protected String md5sum;

	private String viewer;

	private boolean preview;

	private boolean restricted;

	// Explicit catch of CloneNotSupportedException from super.clone()
	@Override
	public Object clone() // NOSONAR
	{
		try
		{
			Attachment clone = (Attachment) super.clone();
			if( data != null )
			{
				clone.data = new HashMap<String, Object>(data);
			}
			return clone;
		}
		catch( CloneNotSupportedException e )
		{
			throw new RuntimeException(e);
		}
	}

	public Attachment()
	{
		super();
		uuid = UUID.randomUUID().toString();
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public void setId(long id)
	{
		this.id = id;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public void setDescription(String description)
	{
		if( description != null && description.length() > 1024 )
		{
			this.description = description.substring(0, 1024);
		}
		else
		{
			this.description = description;
		}
	}

	@Override
	public String getUrl()
	{
		// Stupid Oracle
		return Strings.nullToEmpty(url);
	}

	@Override
	public void setUrl(String url)
	{
		this.url = url;
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

	protected long getLongValue(String value, long def)
	{
		return value == null ? def : Long.parseLong(value);
	}

	protected boolean getBooleanValue(String value)
	{
		return Boolean.parseBoolean(value);
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

	public Item getItem()
	{
		return item;
	}

	public void setItem(Item item)
	{
		this.item = item;
	}

	@Override
	public int hashCode()
	{
		return (int) id;
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
		Attachment other = (Attachment) obj;
		if( id != other.id )
		{
			return false;
		}
		if( id == 0 && other.id == 0 )
		{
			return this == other;
		}
		return true;
	}

	@Override
	public String getViewer()
	{
		return viewer;
	}

	@Override
	public void setViewer(String viewer)
	{
		this.viewer = viewer;
	}

	@Override
	public boolean isPreview()
	{
		return preview;
	}

	@Override
	public void setPreview(boolean preview)
	{
		this.preview = preview;
	}

	@Override
	public boolean isRestricted()
	{
		return restricted;
	}

	@Override
	public void setRestricted(boolean restricted)
	{
		this.restricted = restricted;
	}
}
