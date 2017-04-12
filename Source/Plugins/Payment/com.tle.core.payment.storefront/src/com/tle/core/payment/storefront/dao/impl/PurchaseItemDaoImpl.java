package com.tle.core.payment.storefront.dao.impl;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.tle.beans.item.ItemId;
import com.tle.common.payment.storefront.entity.PurchaseItem;
import com.tle.common.payment.storefront.entity.PurchasedContent;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.payment.storefront.dao.PurchaseItemDao;
import com.tle.core.payment.storefront.dao.PurchasedContentDao;
import com.tle.core.user.CurrentInstitution;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind(PurchaseItemDao.class)
@Singleton
public class PurchaseItemDaoImpl extends GenericDaoImpl<PurchaseItem, Long> implements PurchaseItemDao
{
	@Inject
	PurchasedContentDao purchasedContentDao;

	public PurchaseItemDaoImpl()
	{
		super(PurchaseItem.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PurchaseItem> enumerateForPurchaser(final String buyerId)
	{
		return (List<PurchaseItem>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				return session
					.createQuery(
						"FROM PurchaseItem WHERE purchase.institution = :institution AND purchase.checkoutBy = :buyerId")
					.setParameter("institution", CurrentInstitution.get()).setParameter("buyerId", buyerId).list();
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PurchaseItem> enumerateForItem(final ItemId itemId)
	{
		// itemUuid is being removed.
		// Need to lookup PurchasedContent to find the remote item, and THEN use
		// the remote item UUID to find these
		return (List<PurchaseItem>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				PurchasedContent purchItem = purchasedContentDao.getForLocalItem(itemId);
				return session
					.createQuery(
						"FROM PurchaseItem WHERE purchase.institution = :institution AND sourceItemUuid = :itemUuid")
					.setParameter("institution", CurrentInstitution.get())
					.setParameter("itemUuid", purchItem.getSourceItemUuid()).list();
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PurchaseItem> findActivePurchases(final Store store, final String sourceItemUuid)
	{
		//@formatter:off
		return getHibernateTemplate().executeFind(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				final Query query = session
					.createQuery("FROM PurchaseItem pi"
						+ " WHERE pi.purchase.store = :store"
						+ " AND pi.sourceItemUuid = :sourceItemUuid" 
						+ " AND (pi.subscriptionEndDate IS NULL "
							+ "OR (pi.subscriptionStartDate <= :now AND pi.subscriptionEndDate >= :now))");
				query.setParameter("store", store);
				query.setParameter("sourceItemUuid", sourceItemUuid);
				query.setParameter("now", new Date());
				return query.list();
			}
		});
		//@formatter:on
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PurchaseItem> enumerateForSourceItem(final String uuid)
	{
		return (List<PurchaseItem>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				return session
					.createQuery(
						"FROM PurchaseItem WHERE purchase.institution = :institution AND sourceItemUuid = :sourceUuid")
					.setParameter("institution", CurrentInstitution.get()).setParameter("sourceUuid", uuid).list();
			}
		});
	}

	@Override
	public PurchaseItem get(final String uuid)
	{
		return (PurchaseItem) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				String hql = "from PurchaseItem where uuid = :uuid and purchase.institution = :i";
				Query query = session.createQuery(hql);
				query.setParameter("uuid", uuid);
				query.setParameter("i", CurrentInstitution.get());
				return query.uniqueResult();
			}
		});
	}
}
