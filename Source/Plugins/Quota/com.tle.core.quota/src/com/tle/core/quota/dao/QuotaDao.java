package com.tle.core.quota.dao;

import com.tle.beans.item.Item;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;

public interface QuotaDao extends GenericInstitutionalDao<Item, Long>
{
	long calculateUserFileSize(UserBean user);
}
