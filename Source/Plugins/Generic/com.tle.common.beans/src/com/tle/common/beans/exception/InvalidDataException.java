/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
