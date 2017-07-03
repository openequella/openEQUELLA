package com.tle.core.item.standard.filter;

import java.util.Map;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class RefreshSchemaItemDataFilter extends AbstractRefreshCachedItemDataFilter
{
	private final long schemaId;

	@AssistedInject
	protected RefreshSchemaItemDataFilter(@Assisted long schemaId)
	{
		this.schemaId = schemaId;
	}

	@SuppressWarnings("nls")
	@Override
	public void queryValues(Map<String, Object> values)
	{
		values.put("schemaId", schemaId);
	}

	@SuppressWarnings("nls")
	@Override
	public String getWhereClause()
	{
		return "itemDefinition.schema.id = :schemaId";
	}
}
