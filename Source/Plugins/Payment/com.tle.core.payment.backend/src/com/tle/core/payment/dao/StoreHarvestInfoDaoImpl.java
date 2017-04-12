package com.tle.core.payment.dao;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.payment.entity.StoreHarvestInfo;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.user.CurrentInstitution;

/**
 * @author larry
 */
@SuppressWarnings("nls")
@Bind(StoreHarvestInfoDao.class)
@Singleton
public class StoreHarvestInfoDaoImpl extends GenericDaoImpl<StoreHarvestInfo, Long> implements StoreHarvestInfoDao
{
	public StoreHarvestInfoDaoImpl()
	{
		super(StoreHarvestInfo.class);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void deleteAll()
	{
		for( StoreHarvestInfo hinfo : enumerateAll() )
		{
			delete(hinfo);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<StoreHarvestInfo> enumerateAll()
	{
		return getHibernateTemplate().executeFind(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				Query query = session.createQuery("FROM StoreHarvestInfo s WHERE s.sale.institution = :institution");
				query.setParameter("institution", CurrentInstitution.get());
				query.setCacheable(true);
				query.setReadOnly(true);
				return query.list();
			}
		});
	}
}
