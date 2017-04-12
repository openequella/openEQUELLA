package com.dytech.edge.exceptions;

/**
 * @author Nicholas Read
 */
public class InvalidDateRangeException extends SearchingException
{
	private static final long serialVersionUID = 1L;

	public InvalidDateRangeException()
	{
		super("The specified date range is invalid");
	}
}
