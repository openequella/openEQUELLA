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
