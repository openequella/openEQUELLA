package com.tle.common.workflow;

public class TaskModerator
{
	public enum Type
	{
		USER, GROUP, ROLE
	}

	private final Type type;
	private final String id;
	private final boolean accepted;

	public TaskModerator(String id, Type type, boolean accepted)
	{
		this.id = id;
		this.type = type;
		this.accepted = accepted;
	}

	public Type getType()
	{
		return type;
	}

	public String getId()
	{
		return id;
	}

	public boolean isAccepted()
	{
		return accepted;
	}
}
