package com.tle.core.payment.dao;

import java.util.List;

import com.tle.common.payment.entity.StoreHarvestInfo;
import com.tle.core.hibernate.dao.GenericDao;

/**
 * @author larry
 */
public interface StoreHarvestInfoDao extends GenericDao<StoreHarvestInfo, Long>
{
	void deleteAll();

	List<StoreHarvestInfo> enumerateAll();
}
