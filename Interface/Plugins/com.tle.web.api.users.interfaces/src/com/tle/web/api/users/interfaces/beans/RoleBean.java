package com.tle.web.api.users.interfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tle.web.api.interfaces.beans.AbstractExtendableBean;

@XmlRootElement
public class RoleBean extends AbstractExtendableBean
{
	private final String id;
	private String name;
	private String expression;

	@JsonCreator
	public RoleBean(@JsonProperty("id") String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getExpression()
	{
		return expression;
	}

	public void setExpression(String expression)
	{
		this.expression = expression;
	}
}
