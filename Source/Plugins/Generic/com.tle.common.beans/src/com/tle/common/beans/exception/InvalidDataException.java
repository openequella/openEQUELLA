/*
 * Created on Jun 1, 2005
 */
package com.tle.common.beans.exception;

import java.util.List;
import java.util.Map;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author jmaginnis
 */
public class InvalidDataException extends RuntimeApplicationException
{
	private static final long serialVersionUID = 1L;

	private final List<ValidationError> errors;

	public InvalidDataException(ValidationError error)
	{
		this(Lists.newArrayList(error));
	}

	public InvalidDataException(List<ValidationError> errors)
	{
		this(errors, null);
	}

	public InvalidDataException(List<ValidationError> errors, Throwable cause)
	{
		super(cause);
		this.errors = errors;
	}

	public InvalidDataException(String message, List<ValidationError> errors, Throwable cause)
	{
		super(message, cause);
		this.errors = errors;
	}

	@Override
	public String getMessage()
	{
		StringBuilder msg = new StringBuilder();

		boolean first = true;
		for( ValidationError error : errors )
		{
			if( !first )
			{
				msg.append(", "); //$NON-NLS-1$
				first = false;
			}
			msg.append(error.getField());
			msg.append(": "); //$NON-NLS-1$
			msg.append(error.getMessage());
		}
		return msg.toString();
	}

	/**
	 * @return Returns the errors.
	 */
	public List<ValidationError> getErrors()
	{
		return errors;
	}

	public Map<String, String> getErrorsAsMap()
	{
		Map<String, String> errorMap = Maps.newHashMap();
		for( ValidationError error : errors )
		{
			errorMap.put(error.getField(), error.getMessage());
		}
		return errorMap;
	}
}
