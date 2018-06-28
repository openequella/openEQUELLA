package com.tle.web.scripting.objects;

import com.google.common.collect.Lists;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.scripting.objects.CollectionScriptObject;
import com.tle.common.scripting.types.CollectionScriptType;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.guice.Bind;
import com.tle.web.scripting.ScriptTypeFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Bind(CollectionScriptObject.class)
@Singleton
public class CollectionScriptWrapper extends AbstractScriptWrapper implements CollectionScriptObject
{
	@Inject
	private ScriptTypeFactory scriptTypeFactory;

	@Inject
	private ItemDefinitionService collectionService;

	@Override
	public CollectionScriptType getFromUuid(String uuid)
	{
		ItemDefinition collection = collectionService.getByUuid(uuid);
		if( collection != null )
		{
			return scriptTypeFactory.createCollection(collection);
		}
		return null;
	}

	@Override
	public List<CollectionScriptType> listCollections()
	{
		return Lists.transform(collectionService.enumerateEnabled(), c -> scriptTypeFactory.createCollection(c));
	}
}
