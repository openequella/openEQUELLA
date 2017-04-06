package com.dytech.edge.exceptions;

/**
 * @author aholland
 */
public class ModifyingSystemTypeException extends RuntimeException
{
	public ModifyingSystemTypeException()
	{
		super("You cannot modify or delete a system entity.");
	}
}
