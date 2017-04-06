package com.dytech.edge.exceptions;

/**
 * @author aholland
 */
public class IllegalOperationException extends RuntimeApplicationException
{
	public IllegalOperationException(String why)
	{
		super(why);
	}
}
