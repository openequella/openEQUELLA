package com.tle.common.beans.exception;

import com.dytech.edge.exceptions.RuntimeApplicationException;

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
