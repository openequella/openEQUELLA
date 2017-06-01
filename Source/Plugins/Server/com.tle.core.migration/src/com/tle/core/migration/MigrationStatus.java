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
import java.util.Map;

import com.google.common.collect.Maps;
import com.tle.beans.DatabaseSchema;

@SuppressWarnings("nls")
public class MigrationStatus implements Serializable
{
	public static final String KEY_EXECUTION_STATUS = "subtask";
	public static final String KEY_MIGRATION_INFOS = "migrations";
	public static final String KEY_STATUS = "status";

	private static final long serialVersionUID = 1L;

	private Map<Long, SchemaInfo> schemas = Maps.newHashMap();
	private String exception;
	private boolean needsInstallation;

	public void addSchemaInfo(SchemaInfo schemaInfo)
	{
		schemas.put(schemaInfo.getDatabaseSchema().getId(), schemaInfo);
	}

	public String getException()
	{
		return exception;
	}

	public void setException(String exception)
	{
		this.exception = exception;
	}

	public Map<Long, SchemaInfo> getSchemas()
	{
		return schemas;
	}

	public boolean isNeedsInstallation()
	{
		return needsInstallation;
	}

	public void setNeedsInstallation(boolean needsInstallation)
	{
		this.needsInstallation = needsInstallation;
	}

	public SchemaInfo getSystemSchema()
	{
		return schemas.get(DatabaseSchema.SYSTEM_SCHEMA.getId());
	}

	public void setSchemas(Map<Long, SchemaInfo> newInfos)
	{
		this.schemas = newInfos;
	}

}
