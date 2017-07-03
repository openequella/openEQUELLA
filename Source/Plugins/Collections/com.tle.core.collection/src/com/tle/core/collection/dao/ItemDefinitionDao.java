/*
 * Created on Oct 26, 2005
 */
package com.tle.core.collection.dao;

import java.util.List;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemKey;
import com.tle.core.entity.dao.AbstractEntityDao;

/**
 * @author Nicholas Read
 */
public interface ItemDefinitionDao extends AbstractEntityDao<ItemDefinition>
{
	List<ItemDefinition> findByType(String type);

	List<BaseEntityLabel> listAllForSchema(long schemaID);

	/**
	 * @deprecated Use an event to ask for reference
	 */
	@Deprecated
	List<Class<?>> getReferencingClasses(long id);

	ItemDefinition getByItemId(ItemKey itemId);
}
