/*
 * Created on Oct 26, 2005
 */
package com.tle.core.powersearch;

import java.util.List;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.core.dao.AbstractEntityDao;

/**
 * @author Nicholas Read
 */
public interface PowerSearchDao extends AbstractEntityDao<PowerSearch>
{
	List<Long> enumerateItemdefIds(long powerSearchId);

	List<BaseEntityLabel> listAllForSchema(long schemaID);

	List<PowerSearch> getPowerSearchesReferencingItemDefinition(ItemDefinition itemDefinition);
}
