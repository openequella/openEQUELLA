package com.tle.core.dynacollection;

import java.util.Collection;
import java.util.List;

import com.tle.beans.entity.DynaCollection;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.core.dao.AbstractEntityDao;

public interface DynaCollectionDao extends AbstractEntityDao<DynaCollection>
{
	List<DynaCollection> enumerateForUsage(String usage);

	Collection<DynaCollection> getDynaCollectionsReferencingItemDefinition(ItemDefinition itemDefinition);

	Collection<DynaCollection> getDynaCollectionsReferencingSchema(Schema schema);
}
