package com.tle.web.api.users.interfaces.beans;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tle.web.api.interfaces.beans.AbstractExtendableBean;

@XmlRootElement
public class GroupBean extends AbstractExtendableBean
{
	private String id;
	private String name;
	// I don't like this at all. It should be a GroupBean so we can fill in the
	// links field
	private String parentId;
	// Same here. Imagine a web UI where I want to click on a user result: the
	// web app will need to construct a URL to get the user details, but it's
	// supposed to be able to use the links fields.
	private Set<String> users;

	public GroupBean()
	{
	}

	@JsonIgnore
	public GroupBean(String id)
	{
		this.id = id;
	}

	@JsonIgnore
	public GroupBean(String id, String name)
	{
		this.id = id;
		this.name = name;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public Set<String> getUsers()
	{
		return users;
	}

	public void setUsers(Set<String> users)
	{
		this.users = users;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getParentId()
	{
		return parentId;
	}

	public void setParentId(String parentId)
	{
		this.parentId = parentId;
	}
}
