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

package com.tle.core.integration.beans;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.Institution;
import com.tle.beans.audit.AuditLogTable;

@Entity
@AccessType("field")
public class AuditLogLms implements AuditLogTable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "all_inst")
	@XStreamOmitField
	private Institution institution;

	@Index(name = "all_time")
	private Date timestamp;

	@Column(length = 255, nullable = false)
	@Index(name = "all_user")
	private String userId;

	@Column(length = 40)
	@Index(name = "all_session")
	private String sessionId;

	@Index(name = "all_uuid")
	@Column(length = 40)
	private String uuid;

	@Index(name = "all_version")
	private int version;

	@Index(name = "all_type")
	private char type;

	@Index(name = "all_selected")
	@Column(length = 255)
	private String selected;

	@Index(name = "all_contType")
	@Column(length = 120)
	private String contentType;

	@Index(name = "all_resource")
	@Column(length = 512, name = "`resource`")
	private String resource;

	@Index(name = "all_latest")
	private boolean latest;

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
	public Institution getInstitution()
	{
		return institution;
	}

	@Override
	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	@Override
	public Date getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}

	@Override
	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	@Override
	public String getSessionId()
	{
		return sessionId;
	}

	public void setSessionId(String sessionId)
	{
		this.sessionId = sessionId;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public char getType()
	{
		return type;
	}

	public void setType(char type)
	{
		this.type = type;
	}

	public String getSelected()
	{
		return selected;
	}

	public void setSelected(String selected)
	{
		this.selected = trunc(selected, 255);
	}

	public String getResource()
	{
		return resource;
	}

	public void setResource(String resource)
	{
		this.resource = trunc(resource, 512);
	}

	public String getContentType()
	{
		return contentType;
	}

	public void setContentType(String contentType)
	{
		this.contentType = trunc(contentType, 120);
	}

	private String trunc(String str, int len)
	{
		if( str == null || str.length() <= len )
		{
			return str;
		}
		return str.substring(0, len);
	}

	public boolean isLatest()
	{
		return latest;
	}

	public void setLatest(boolean latest)
	{
		this.latest = latest;
	}

}
