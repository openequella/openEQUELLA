package com.tle.web.api.institution.interfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InstitutionBean
{
	private long uniqueId;
	private String name;
	private String filestoreId;
	private String url;
	private String password;
	private String timeZone;
	private boolean enabled = true;

	public long getUniqueId()
	{
		return uniqueId;
	}

	public void setUniqueId(long uniqueId)
	{
		this.uniqueId = uniqueId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getFilestoreId()
	{
		return filestoreId;
	}

	public void setFilestoreId(String filestoreId)
	{
		this.filestoreId = filestoreId;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getTimeZone()
	{
		return timeZone;
	}

	public void setTimeZone(String timeZone)
	{
		this.timeZone = timeZone;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

}
