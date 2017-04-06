package com.tle.core.payment.dao.impl;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.StoreFront;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.payment.SaleSearchResults;
import com.tle.core.payment.dao.SaleDao;
import com.tle.core.user.CurrentInstitution;

@SuppressWarnings("nls")
@Bind(SaleDao.class)
@Singleton
public class SaleDaoImpl extends GenericInstitionalDaoImpl<Sale, Long> implements SaleDao
{
	public SaleDaoImpl()
	{
		super(Sale.class);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void deleteAll()
	{
		for( Sale sale : enumerateAll() )
		{
			delete(sale);
		}
	}

	@Override
	public Sale get(String uuid)
	{
		return findByCriteria(Restrictions.eq("institution", CurrentInstitution.get()), Restrictions.eq("uuid", uuid));
	}

	@Override
	public Sale getByReceipt(String receipt)
	{
		return findByCriteria(Restrictions.eq("institution", CurrentInstitution.get()),
			Restrictions.eq("receipt", receipt));
	}

	@Override
	public List<Sale> enumerateForStoreFront(StoreFront storeFront)
	{
		return findAllByCriteria(Restrictions.eq("storefront", storeFront));
	}

	@Override
	public SaleSearchResults search(StoreFront storeFront, int offset, int count, String customerReference)
	{
		final List<Criterion> crit = Lists.newArrayList();
		crit.add(Restrictions.eq("storefront", storeFront));
		if( !Strings.isNullOrEmpty(customerReference) )
		{
			crit.add(Restrictions.eq("customerReference", customerReference));
		}

		final Criterion[] critArray = crit.toArray(new Criterion[crit.size()]);
		final List<Sale> sales = findAllByCriteria(Order.desc("creationDate"), offset, count, critArray);

		final SaleSearchResults result = new SaleSearchResults();
		result.setResults(sales);
		result.setOffset(offset);
		result.setCount(sales.size());
		result.setAvailable((int) countByCriteria(critArray));
		return result;
	}

	@Override
	public Sale getByCustomerRef(String refUuid)
	{
		return findByCriteria(Restrictions.eq("institution", CurrentInstitution.get()),
			Restrictions.eq("customerReference", refUuid));
	}
}
