package com.tle.core.institution.impl;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.tle.beans.Institution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.institution.InstitutionDao;

@Singleton
@SuppressWarnings("nls")
@Bind(InstitutionDao.class)
public class InstitutionDaoImpl extends GenericDaoImpl<Institution, Long> implements InstitutionDao
{
	public InstitutionDaoImpl()
	{
		super(Institution.class);
	}

	@Override
	public Institution findByUniqueId(long uniqueId)
	{
		return findByCriteria(Restrictions.eq("uniqueId", uniqueId));
	}
}
