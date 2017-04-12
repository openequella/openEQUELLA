/*
 * Created on Dec 7, 2004
 */
package com.dytech.edge.exceptions;

/**
 * @author Nicholas Read
 */
public class InUseException extends RuntimeApplicationException
{
	public InUseException(String inUseBy)
	{
		super(inUseBy);
	}
}
