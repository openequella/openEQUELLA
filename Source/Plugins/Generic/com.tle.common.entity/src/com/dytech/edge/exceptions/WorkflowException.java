package com.dytech.edge.exceptions;

/**
 * Represents an exception occuring during workflow.
 */
public class WorkflowException extends RuntimeApplicationException
{
	private static final long serialVersionUID = 1L;

	public WorkflowException(String msg)
	{
		super(msg);
	}

	public WorkflowException(String msg, Throwable t)
	{
		super(msg, t);
	}

	public WorkflowException(Throwable t)
	{
		super(t);
	}
}
