package com.tle.core.payment.dao.impl;

import javax.inject.Singleton;

import com.tle.common.payment.entity.TaxType;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.payment.dao.TaxDao;

@Bind(TaxDao.class)
@Singleton
public class TaxDaoImpl extends AbstractEntityDaoImpl<TaxType> implements TaxDao
{
	public TaxDaoImpl()
	{
		super(TaxType.class);
	}

	/*
	 * @SuppressWarnings("unchecked")
	 * @Override public List<TaxType> enumerateForStoreFront(final StoreFront
	 * sf) { return getHibernateTemplate().executeFind(new
	 * TLEHibernateCallback() {
	 * @Override public Object doInHibernate(Session session) throws
	 * HibernateException { Query query = session.createQuery(
	 * "SELECT StoreFront.taxType FROM StoreFront sf WHERE sf = :sf");
	 * query.setParameter("sf", sf); query.setCacheable(true);
	 * query.setReadOnly(true); List<TaxType> res = query.list(); return res; }
	 * }); }
	 */
}
