/*
 * Created on Jun 1, 2005
 */
package com.tle.common.beans.exception;

import java.io.Serializable;

/**
 * @author jmaginnis
 */
public class ValidationError implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final String field;
	private final String message;
	private final String key;

	public ValidationError(String field, String message)
	{
		this(field, message, null);
	}

	public ValidationError(String field, String message, String key)
	{
		this.field = field;
		this.message = message;
		this.key = key;
	}

	public String getField()
	{
		return field;
	}

	public String getMessage()
	{
		return message;
	}

	public String getKey()
	{
		return key;
	}
}
