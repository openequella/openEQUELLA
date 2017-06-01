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

package com.tle.web.bulk.operation;

import java.io.Serializable;

public class BulkResult implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final boolean succeeded;
	private String name;
	private String reason;

	public BulkResult(boolean succeeded, String name, String reason)
	{
		this.name = name;
		this.reason = reason;
		this.succeeded = succeeded;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getReason()
	{
		return reason;
	}

	public void setReason(String reason)
	{
		this.reason = reason;
	}

	public boolean isSucceeded()
	{
		return succeeded;
	}
}