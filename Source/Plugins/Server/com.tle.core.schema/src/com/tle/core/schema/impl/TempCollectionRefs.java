package com.tle.core.schema.impl;

import java.util.List;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.core.guice.Bind;
import com.tle.core.schema.SchemaReferences;
import com.tle.core.services.entity.ItemDefinitionService;

@Bind
@Singleton
public class TempCollectionRefs implements SchemaReferences
{

	@Inject
	private ItemDefinitionService collectionService;

	@Override
	public List<BaseEntityLabel> getSchemaUses(long id)
	{
		return collectionService.listAllForSchema(id);
	}
}
