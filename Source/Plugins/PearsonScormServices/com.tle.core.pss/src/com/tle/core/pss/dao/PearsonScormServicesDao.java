package com.tle.core.pss.dao;

import com.tle.beans.item.Item;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;
import com.tle.core.pss.entity.PssCallbackLog;

public interface PearsonScormServicesDao extends GenericInstitutionalDao<PssCallbackLog, Long>
{
	PssCallbackLog getByTrackingNo(int trackingNumber);

	PssCallbackLog getByItem(Item item);

	void deleteForItem(Item item);
}
