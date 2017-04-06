package com.tle.core.dao;

import com.tle.beans.Staging;
import com.tle.core.hibernate.dao.GenericDao;

public interface StagingDao extends GenericDao<Staging, String>
{
	void deleteAllForUserSession(String userSession);
}
