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

package com.tle.beans;

import com.tle.common.Check;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@SuppressWarnings("nls")
@Table(name = DatabaseSchema.TABLE_NAME)
public class DatabaseSchema implements Serializable
{
	private static final long serialVersionUID = -1L;

	public static final String TABLE_NAME = "sys_database_schema";

	public static final DatabaseSchema SYSTEM_SCHEMA = new DatabaseSchema(-1);

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@Column(length = 512)
	private String url;
	@Column(length = 64)
	private String username;
	@Column(length = 64)
	private String password;
	@Column(length = 512)
	private String reportingUrl;
	@Column(length = 64)
	private String reportingUsername;
	@Column(length = 64)
	private String reportingPassword;
	@Column(name = "`online`")
	private boolean online;
	@Lob
	private String description;
	private boolean useSystem;

	public DatabaseSchema()
	{
		// nothing
	}

	public DatabaseSchema(long id)
	{
		this.id = id;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getReportingUrl()
	{
		return reportingUrl;
	}

	public void setReportingUrl(String reportingUrl)
	{
		this.reportingUrl = reportingUrl;
	}

	public String getReportingUsername()
	{
		return reportingUsername;
	}

	public void setReportingUsername(String reportingUsername)
	{
		this.reportingUsername = reportingUsername;
	}

	public String getReportingPassword()
	{
		return reportingPassword;
	}

	public void setReportingPassword(String reportingPassword)
	{
		this.reportingPassword = reportingPassword;
	}

	public boolean isOnline()
	{
		return online;
	}

	public void setOnline(boolean online)
	{
		this.online = online;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public int hashCode()
	{
		return Long.valueOf(id).hashCode();
	}

	@Override
	public String toString()
	{
		String str = username + '@' + url;
		if( !Check.isEmpty(description) )
		{
			return '\'' + description + "' " + str;
		}
		return str;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof DatabaseSchema) )
		{
			return false;
		}

		return id == ((DatabaseSchema) obj).id;
	}

	public boolean isSystem()
	{
		return id == -1;
	}

	public boolean isUseSystem()
	{
		return useSystem;
	}

	public void setUseSystem(boolean useSystem)
	{
		this.useSystem = useSystem;
	}
}
