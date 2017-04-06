package com.tle.core.harvester;

import java.util.Date;

import com.tle.common.harvester.HarvesterProfile;
import com.tle.core.dao.AbstractEntityDao;

public interface HarvesterProfileDao extends AbstractEntityDao<HarvesterProfile>
{
	void updateLastRun(HarvesterProfile profile, Date date);
}
