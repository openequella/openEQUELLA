package com.tle.core.connectors.canvas.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
public class CanvasAccountBean
{
	private String id;
	@JsonProperty("root_account_id")
	private String rootAccount;
	private String name;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getRootAccount()
	{
		return rootAccount;
	}

	public void setRootAccount(String rootAccount)
	{
		this.rootAccount = rootAccount;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
