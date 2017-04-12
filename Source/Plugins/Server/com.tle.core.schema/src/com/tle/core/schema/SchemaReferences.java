package com.tle.core.schema;

import java.util.List;

import com.tle.beans.entity.BaseEntityLabel;

public interface SchemaReferences
{
	List<BaseEntityLabel> getSchemaUses(long id);
}
