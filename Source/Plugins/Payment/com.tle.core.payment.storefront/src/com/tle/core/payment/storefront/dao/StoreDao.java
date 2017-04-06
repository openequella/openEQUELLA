package com.tle.core.payment.storefront.dao;

import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.dao.AbstractEntityDao;

public interface StoreDao extends AbstractEntityDao<Store>
{

	Long countOrderPartsForStore(Store store);
}
