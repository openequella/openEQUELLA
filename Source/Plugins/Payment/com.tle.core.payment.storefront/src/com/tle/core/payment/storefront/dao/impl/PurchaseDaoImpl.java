package com.tle.core.payment.storefront.dao.impl;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.beans.item.ItemId;
import com.tle.common.payment.storefront.entity.Purchase;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.payment.storefront.dao.PurchaseDao;
import com.tle.core.user.CurrentInstitution;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind(PurchaseDao.class)
@Singleton
public class PurchaseDaoImpl extends GenericInstitionalDaoImpl<Purchase, Long> implements PurchaseDao
{
	public PurchaseDaoImpl()
	{
		super(Purchase.class);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void deleteAll()
	{
		for( Purchase order : enumerateAll() )
		{
			delete(order);
		}
	}

	@Override
	@Transactional
	public List<String> enumerateCheckoutByforItem(final ItemId itemId)
	{
		List<Purchase> purchases = enumerateAll(new ListCallback()
		{

			@Override
			public String getAdditionalJoins()
			{
				return "join be.purchaseItems pi";
			}

			@Override
			public String getAdditionalWhere()
			{
				return "pi.sourceItemUuid = :uuid ";
			}

			@Override
			public String getOrderBy()
			{
				return "ORDER BY be.checkoutDate DESC";
			}

			@Override
			public void processQuery(Query query)
			{
				query.setString("uuid", itemId.getUuid());
			}

			@Override
			public boolean isDistinct()
			{
				return false;
			}
		});

		return Lists.transform(purchases, new Function<Purchase, String>()
		{
			@Override
			public String apply(Purchase purchase)
			{
				return purchase.getCheckoutBy();
			}
		});
	}

	@Override
	public void updateCheckoutUser(final String fromUserId, final String toUserId)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query q = session.createQuery("UPDATE Purchase SET checkoutBy = :toUser WHERE checkoutBy = :fromUser"
					+ " AND institution = :institution");
				q.setParameter("fromUser", fromUserId);
				q.setParameter("toUser", toUserId);
				q.setParameter("institution", CurrentInstitution.get());
				q.executeUpdate();
				return null;
			}
		});
	}

	@Override
	public void updatePaidByUser(final String fromUserId, final String toUserId)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query q = session.createQuery("UPDATE Purchase SET paidForBy = :toUser WHERE paidForBy = :fromUser"
					+ " AND institution = :institution");
				q.setParameter("fromUser", fromUserId);
				q.setParameter("toUser", toUserId);
				q.setParameter("institution", CurrentInstitution.get());
				q.executeUpdate();
				return null;
			}
		});
	}
}
