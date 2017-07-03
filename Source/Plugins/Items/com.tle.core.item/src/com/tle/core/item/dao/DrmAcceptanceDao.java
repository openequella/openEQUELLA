package com.tle.core.item.dao;

import com.tle.beans.item.DrmAcceptance;
import com.tle.core.hibernate.dao.GenericDao;

/**
 * @author Nicholas Read
 */
public interface DrmAcceptanceDao extends GenericDao<DrmAcceptance, Long>
{
	void userIdChanged(String fromUserId, String toUserId);
}
