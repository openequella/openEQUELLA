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
