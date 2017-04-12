package com.tle.beans;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.tle.common.Check;

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
