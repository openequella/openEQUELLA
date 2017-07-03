package com.tle.core.settings.dao;

import java.util.Collection;

import com.tle.beans.ConfigurationProperty;
import com.tle.beans.ConfigurationProperty.PropertyKey;
import com.tle.core.hibernate.dao.GenericDao;

public interface ConfigurationDao extends GenericDao<ConfigurationProperty, PropertyKey>
{
	void deleteAll();

	void deletePropertiesLike(Collection<String> select);
}
