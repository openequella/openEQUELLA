package com.tle.core.collection.service;

import java.util.Collection;
import java.util.List;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemKey;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.remoting.RemoteItemDefinitionService;

/**
 * @author Nicholas Read
 */
public interface ItemDefinitionService
	extends
		AbstractEntityService<EntityEditingBean, ItemDefinition>,
		RemoteItemDefinitionService
{
	List<ItemDefinition> enumerateForType(String type);

	List<ItemDefinition> enumerateForWorkflow(long workflowID);

	List<ItemDefinition> enumerateWithWorkflow();

	List<ItemDefinition> enumerateCreateable();

	List<ItemDefinition> enumerateSearchable();

	List<BaseEntityLabel> listAllForSchema(long schemaID);

	List<BaseEntityLabel> listSearchable();

	List<BaseEntityLabel> listCreateable();

	Collection<ItemDefinition> filterSearchable(Collection<ItemDefinition> collections);

	List<ItemDefinition> getMatchingSearchable(Collection<Long> itemdefs);

	List<ItemDefinition> getMatchingSearchableUuid(Collection<String> itemdefUuids);

	List<ItemDefinition> getMatchingCreatableUuid(Collection<String> itemdefs);

	ItemDefinition getForItemCreate(String uuid);

	ItemDefinition getByItemIdUnsecure(ItemKey itemId);
}
