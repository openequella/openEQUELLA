package com.tle.core.payment.storefront.dao.impl;

import javax.inject.Singleton;

import org.hibernate.Query;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.payment.storefront.dao.StoreDao;

@Bind(StoreDao.class)
@Singleton
public class StoreDaoImpl extends AbstractEntityDaoImpl<Store> implements StoreDao
{
	public StoreDaoImpl()
	{
		super(Store.class);
	}

	@Override
	public Long countOrderPartsForStore(final Store store)
	{
		Long count = (Long) getHibernateTemplate().execute(new HibernateCallback()
		{
			@SuppressWarnings("nls")
			@Override
			public Object doInHibernate(org.hibernate.Session session)
			{
				String queryString = "select count(*) from OrderStorePart where store = :store";
				Query countQuery = session.createQuery(queryString);
				countQuery.setParameter("store", store);
				return countQuery.uniqueResult();
			}
		});
		return count;
	}
}