package com.tle.core.connectors.canvas.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
public class CanvasCourseBean
{
	private String id;
	private String name;
	@JsonProperty("course_code")
	private String code;
	@JsonProperty("workflow_state")
	private String state;
	@JsonProperty("root_account_id")
	private String rootAccountId;
	@JsonProperty("account_id")
	private String accountId;

	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
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

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getAccountId()
	{
		return accountId;
	}

	public void setAccountId(String accountId)
	{
		this.accountId = accountId;
	}

	public String getRootAccountId()
	{
		return rootAccountId;
	}

	public void setRootAccountId(String rootAccountId)
	{
		this.rootAccountId = rootAccountId;
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean equals = super.equals(obj);
		if( obj instanceof CanvasCourseBean )
		{
			equals |= ((CanvasCourseBean) obj).getId().equals(this.getId());
		}
		return equals;
	}
}
