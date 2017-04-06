package com.tle.core.workflow.filters;

import java.util.Map;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class RefreshCollectionItemDataFilter extends AbstractRefreshCachedItemDataFilter
{
	private final long collectionId;

	@AssistedInject
	protected RefreshCollectionItemDataFilter(@Assisted long collectionId)
	{
		this.collectionId = collectionId;
	}

	@SuppressWarnings("nls")
	@Override
	public void queryValues(Map<String, Object> values)
	{
		values.put("collectionId", collectionId);
	}

	@SuppressWarnings("nls")
	@Override
	public String getWhereClause()
	{
		return "itemDefinition.id = :collectionId";
	}
}
