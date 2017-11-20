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

import com.dytech.edge.exceptions.RuntimeApplicationException;

/**
 * @author Nicholas Read
 */
public class NotFoundException extends RuntimeApplicationException
{
	private static final long serialVersionUID = 1L;
	private boolean fromRequest;

	public NotFoundException(boolean fromRequest)
	{
		this.fromRequest = fromRequest;
	}

	public NotFoundException(String message)
	{
		this(message, false);
	}

	public NotFoundException(String message, boolean fromRequest)
	{
		super(message);
		setShowStackTrace(false);
		this.fromRequest = fromRequest;
	}

	public NotFoundException(String message, Throwable cause, boolean fromRequest)
	{
		super(message, cause);
		setShowStackTrace(false);
		this.fromRequest = fromRequest;
	}

	public boolean isFromRequest()
	{
		return fromRequest;
	}

	public void setFromRequest(boolean fromRequest)
	{
		this.fromRequest = fromRequest;
	}

	@Override
	public boolean isShowStackTrace()
	{
		return false;
	}
}
