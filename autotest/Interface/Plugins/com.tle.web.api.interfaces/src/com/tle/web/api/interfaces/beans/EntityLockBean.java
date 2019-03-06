package com.tle.web.api.interfaces.beans;

import java.util.Map;

public class EntityLockBean
{
	private String uuid;
	private UserBean owner;
	private Map<String, String> links;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public UserBean getOwner()
	{
		return owner;
	}

	public void setOwner(UserBean owner)
	{
		this.owner = owner;
	}

	public Map<String, String> getLinks()
	{
		return links;
	}

	public void setLinks(Map<String, String> links)
	{
		this.links = links;
	}
}
