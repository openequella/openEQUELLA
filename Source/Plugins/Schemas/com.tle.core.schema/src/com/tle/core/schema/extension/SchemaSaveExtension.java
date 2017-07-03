package com.tle.core.schema.extension;

import com.tle.annotation.Nullable;
import com.tle.beans.entity.Schema;

public interface SchemaSaveExtension
{
	void schemaSaved(@Nullable Schema oldSchema, Schema newSchema);
}
