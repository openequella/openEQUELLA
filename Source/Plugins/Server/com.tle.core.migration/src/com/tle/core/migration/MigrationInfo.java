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

import com.tle.common.i18n.InternalI18NString;
import com.tle.common.i18n.KeyString;

public class MigrationInfo implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final InternalI18NString name;
	private final InternalI18NString description;
	private boolean failed;
	private boolean executing;
	private boolean canRetry;
	private boolean processed;
	private String migrateId;
	private String error;

	public MigrationInfo(String nameKey)
	{
		this(nameKey, nameKey);
	}

	public MigrationInfo(String nameKey, String descriptionKey)
	{
		this(new KeyString(nameKey), new KeyString(descriptionKey));
	}

	public MigrationInfo(InternalI18NString name)
	{
		this(name, name);
	}

	public MigrationInfo(InternalI18NString name, InternalI18NString description)
	{
		this.name = name;
		name.toString();
		this.description = description;
	}

	public void setError(String error)
	{
		this.error = error;
	}

	public String getError()
	{
		return error;
	}

	public boolean isFailed()
	{
		return failed;
	}

	public void setFailed(boolean failed)
	{
		this.failed = failed;
	}

	public boolean isCanRetry()
	{
		return canRetry;
	}

	public void setCanRetry(boolean canRetry)
	{
		this.canRetry = canRetry;
	}

	public boolean isExecuting()
	{
		return executing;
	}

	public void setExecuting(boolean executing)
	{
		this.executing = executing;
	}

	public boolean isProcessed()
	{
		return processed;
	}

	public void setProcessed(boolean processed)
	{
		this.processed = processed;
	}

	public String getMigrateId()
	{
		return migrateId;
	}

	public void setMigrateId(String migrateId)
	{
		this.migrateId = migrateId;
	}

	public InternalI18NString getName()
	{
		return name;
	}

	public InternalI18NString getDescription()
	{
		return description;
	}
}
