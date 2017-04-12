package com.tle.beans.security;

import java.io.Serializable;

public class ACLEntryMapping implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final long id;
	private final char grant;
	private final int priority;
	private final String target;
	private final String expression;

	public ACLEntryMapping(long id, char grant, int priority, String target, String expression)
	{
		this.id = id;
		this.grant = grant;
		this.priority = priority;
		this.target = target;
		this.expression = expression;
	}

	public int getPriority()
	{
		return priority;
	}

	public String getTarget()
	{
		return target;
	}

	public String getExpression()
	{
		return expression;
	}

	public char getGrant()
	{
		return grant;
	}

	public long getId()
	{
		return id;
	}
}
