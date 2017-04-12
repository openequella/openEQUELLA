package com.tle.core.payment.dao;

import java.util.List;

import com.tle.beans.entity.DynaCollection;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.Region;
import com.tle.core.dao.AbstractEntityDao;

public interface CatalogueDao extends AbstractEntityDao<Catalogue>
{
	List<Catalogue> enumerateByRegion(Region region, boolean enabledOnly);

	boolean isExistingReferences(DynaCollection dynaCollection);
}
