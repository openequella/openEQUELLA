package com.tle.core.payment.dao;

import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;

/**
 * @author aholland
 */
public interface SubscriptionPeriodDao extends GenericInstitutionalDao<SubscriptionPeriod, Long>
{
	void deleteAll();

	SubscriptionPeriod getByUuid(String uuid);
}
