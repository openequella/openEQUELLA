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

package com.tle.core.migration;

import java.io.Serializable;

public class MigrationStatusLog implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum LogType
	{
		MESSAGE, SQL, WARNING
	}

	private final LogType type;
	private boolean failed;
	private String key;
	private Object[] values;

	public MigrationStatusLog(String sql, boolean failed)
	{
		this.type = LogType.SQL;
		this.failed = failed;
		this.values = new String[]{sql};
	}

	public MigrationStatusLog(String key, Object... values)
	{
		this(LogType.MESSAGE, key, values);
	}

	public MigrationStatusLog(LogType type, String key, Object... values)
	{
		this.key = key;
		this.values = values;
		this.type = type;
	}

	public LogType getType()
	{
		return type;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public Object[] getValues()
	{
		return values;
	}

	public void setValues(Object[] values)
	{
		this.values = values;
	}

	public boolean isFailed()
	{
		return failed;
	}

	public void setFailed(boolean failed)
	{
		this.failed = failed;
	}
}
