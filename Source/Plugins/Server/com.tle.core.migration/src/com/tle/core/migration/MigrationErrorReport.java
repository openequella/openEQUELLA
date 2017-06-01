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
import java.util.List;

import com.tle.common.i18n.InternalI18NString;

public class MigrationErrorReport implements Serializable
{
	private static final long serialVersionUID = 1L;

	private InternalI18NString name;
	private String message;
	private String error;
	private boolean canRetry;

	private String databaseType;
	private List<MigrationStatusLog> log;

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getError()
	{
		return error;
	}

	public void setError(String error)
	{
		this.error = error;
	}

	public String getDatabaseType()
	{
		return databaseType;
	}

	public void setDatabaseType(String databaseType)
	{
		this.databaseType = databaseType;
	}

	public InternalI18NString getName()
	{
		return name;
	}

	public void setName(InternalI18NString name)
	{
		this.name = name;
	}

	public boolean isCanRetry()
	{
		return canRetry;
	}

	public void setCanRetry(boolean canRetry)
	{
		this.canRetry = canRetry;
	}

	public List<MigrationStatusLog> getLog()
	{
		return log;
	}

	public void setLog(List<MigrationStatusLog> log)
	{
		this.log = log;
	}
}
