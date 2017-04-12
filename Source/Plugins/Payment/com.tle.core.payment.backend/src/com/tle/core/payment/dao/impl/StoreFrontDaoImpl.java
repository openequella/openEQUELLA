package com.tle.core.payment.dao.impl;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.payment.entity.StoreFront;
import com.tle.common.payment.entity.TaxType;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.payment.dao.StoreFrontDao;

@Bind(StoreFrontDao.class)
@Singleton
public class StoreFrontDaoImpl extends AbstractEntityDaoImpl<StoreFront> implements StoreFrontDao
{
	public StoreFrontDaoImpl()
	{
		super(StoreFront.class);
	}

	@SuppressWarnings("nls")
	@Override
	public StoreFront getByOAuthClient(final OAuthClient client)
	{
		return uniqueResult(findAllByCriteria(Restrictions.eq("client", client)));
	}

	@SuppressWarnings("nls")
	@Override
	public List<StoreFront> enumerateForTaxType(TaxType taxType)
	{
		return findAllByCriteria(Restrictions.eq("taxType", taxType));
	}

	@Override
	public Long countSalesForStoreFront(final StoreFront storeFront)
	{
		Long count = (Long) getHibernateTemplate().execute(new HibernateCallback()
		{
			@SuppressWarnings("nls")
			@Override
			public Object doInHibernate(org.hibernate.Session session)
			{
				// NB: property name of Sale is "storefront" one word lower case
				String queryString = "select Count(*) From Sale s where s.storefront = :storeFront";
				Query countQuery = session.createQuery(queryString);
				countQuery.setParameter("storeFront", storeFront);
				return countQuery.uniqueResult();
			}
		});
		return count;
	}
}
