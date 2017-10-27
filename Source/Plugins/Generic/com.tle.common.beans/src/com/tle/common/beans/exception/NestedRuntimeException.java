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

public abstract class NestedRuntimeException extends RuntimeException
{
	public NestedRuntimeException(String msg)
	{
		super(msg);
	}

	public NestedRuntimeException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

	@SuppressWarnings("nls")
	@Override
	public String getMessage()
	{
		Throwable cause = getCause();
		String message = super.getMessage();
		if( cause != null )
		{
			StringBuilder buf = new StringBuilder();
			if( message != null )
			{
				buf.append(message).append("; ");
			}
			buf.append("nested exception is ").append(cause);
			return buf.toString();
		}
		else
		{
			return message;
		}
	}
}
