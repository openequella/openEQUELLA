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

package com.dytech.edge.exceptions;

/**
 * @author Nicholas Read
 */
public class InvalidSearchQueryException extends SearchingException
{
	private static final long serialVersionUID = 1L;

	public InvalidSearchQueryException(String query)
	{
		super(query);
	}

	public InvalidSearchQueryException(String query, Throwable t)
	{
		super(query, t);
	}

	@Override
	public String getLocalizedMessage()
	{
		return "The search query '" + getMessage() + "' is invalid"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
