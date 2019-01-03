/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.core.entity;

import com.tle.common.Pair;

import java.util.*;

public class EnumerateOptions
{
	private Boolean includeSystem;
	private Boolean includeDisabled;
	private String query;
	private int offset;
	private int max = -1;

	private Map<String, Object> parameters = new HashMap<>();

	public EnumerateOptions()
	{
	}

	public EnumerateOptions(String query, int offset, int max, Boolean includeSystem, Boolean includeDisabled)
	{
		this.query = query;
		this.offset = offset;
		this.max = max;
		this.includeSystem = includeSystem;
		this.includeDisabled = includeDisabled;
	}

	public EnumerateOptions addParameter(String name, Object value)
	{
		parameters.put(name, value);
		return this;
	}

	public Boolean isIncludeSystem()
	{
		return includeSystem;
	}

	public EnumerateOptions setIncludeSystem(boolean includeSystem)
	{
		this.includeSystem = includeSystem;
		return this;
	}

	public Boolean isIncludeDisabled()
	{
		return includeDisabled;
	}

	public EnumerateOptions setIncludeDisabled(boolean includeDisabled)
	{
		this.includeDisabled = includeDisabled;
		return this;
	}

	public String getQuery()
	{
		return query;
	}

	public EnumerateOptions setQuery(String query)
	{
		this.query = query;
		return this;
	}

	public int getOffset()
	{
		return offset;
	}

	public EnumerateOptions setOffset(int offset)
	{
		this.offset = offset;
		return this;
	}

	public int getMax()
	{
		return max;
	}

	public EnumerateOptions setMax(int max)
	{
		this.max = max;
		return this;
	}

	public Map<String, Object> getParameters()
	{
		return Collections.unmodifiableMap(parameters);
	}
}
