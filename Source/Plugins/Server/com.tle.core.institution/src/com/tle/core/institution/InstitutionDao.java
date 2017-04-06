package com.tle.core.institution;

import com.tle.beans.Institution;
import com.tle.core.hibernate.dao.GenericDao;

public interface InstitutionDao extends GenericDao<Institution, Long>
{
	Institution findByUniqueId(long uniqueId);
}
