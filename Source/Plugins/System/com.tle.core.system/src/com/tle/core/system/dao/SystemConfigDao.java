package com.tle.core.system.dao;

import com.tle.core.hibernate.dao.GenericDao;
import com.tle.core.migration.beans.SystemConfig;

public interface SystemConfigDao extends GenericDao<SystemConfig, String>
{
	String getConfig(String key);

	void updateConfig(String key, String value);

	long getAndIncrement(String key);

	void increaseToAtLeast(String key, long value);
}
