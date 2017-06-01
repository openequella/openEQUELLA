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

package com.tle.core.harvester.oai.error;

import com.tle.core.harvester.oai.data.OAIError;

/**
 * 
 */
public class OAIException extends Exception
{
	protected String code;
	protected String errorMessage;

	public OAIException(String code, String message)
	{
		this.code = code;
		this.errorMessage = message;
	}

	public OAIException(String code, Throwable t)
	{
		super(t);
		this.code = code;
		this.errorMessage = t.getMessage();
	}

	public OAIException(OAIError error)
	{
		super(error.getMessage());
	}

	public String getCode()
	{
		return code;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}
}
