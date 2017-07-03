package com.tle.core.filesystem.staging.dao;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.Staging;
import com.tle.core.hibernate.dao.GenericDao;

@NonNullByDefault
public interface StagingDao extends GenericDao<Staging, String>
{
	void deleteAllForUserSession(String userSession);
}
