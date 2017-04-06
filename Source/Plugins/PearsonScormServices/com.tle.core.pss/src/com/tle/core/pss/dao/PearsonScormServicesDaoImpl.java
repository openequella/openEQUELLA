package com.tle.core.pss.dao;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.pss.entity.PssCallbackLog;
import com.tle.core.user.CurrentInstitution;

@Bind(PearsonScormServicesDao.class)
@Singleton
public class PearsonScormServicesDaoImpl extends GenericInstitionalDaoImpl<PssCallbackLog, Long>
	implements
		PearsonScormServicesDao
{
	public PearsonScormServicesDaoImpl()
	{
		super(PssCallbackLog.class);
	}

	@Override
	public PssCallbackLog getByTrackingNo(int trackingNumber)
	{
		return findByCriteria(Restrictions.eq("institution", CurrentInstitution.get()),
			Restrictions.eq("trackingNumber", trackingNumber));
	}

	@Override
	public PssCallbackLog getByItem(Item item)
	{
		return findByCriteria(Restrictions.eq("institution", CurrentInstitution.get()), Restrictions.eq("item", item));
	}

	@Override
	public void deleteForItem(Item item)
	{
		List<PssCallbackLog> entries = findAllByCriteria(Restrictions.eq("institution", CurrentInstitution.get()),
			Restrictions.eq("item", item));
		getHibernateTemplate().deleteAll(entries);
	}
}