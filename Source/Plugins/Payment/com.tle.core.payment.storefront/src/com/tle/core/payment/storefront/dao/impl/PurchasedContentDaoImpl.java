package com.tle.core.payment.storefront.dao.impl;

import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tle.beans.item.ItemId;
import com.tle.common.payment.storefront.entity.PurchasedContent;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.payment.storefront.dao.PurchasedContentDao;
import com.tle.core.user.CurrentInstitution;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind(PurchasedContentDao.class)
@Singleton
public class PurchasedContentDaoImpl extends GenericInstitionalDaoImpl<PurchasedContent, Long>
	implements
		PurchasedContentDao
{
	public PurchasedContentDaoImpl()
	{
		super(PurchasedContent.class);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void deleteAll()
	{
		for( PurchasedContent pc : enumerateAll() )
		{
			delete(pc);
		}
	}

	@Override
	public List<PurchasedContent> enumerateForUser(String userId)
	{
		return enumerateAll(new BuyerCallback(userId));
	}

	@Override
	@Transactional
	public PurchasedContent getForSourceItem(Store store, ItemId itemId)
	{
		return findByCriteria(Restrictions.eq("institution", CurrentInstitution.get()),
			Restrictions.eq("sourceItemUuid", itemId.getUuid()),
			Restrictions.eq("sourceItemVersion", itemId.getVersion()), Restrictions.eq("store", store));
	}

	@Override
	@Transactional
	public PurchasedContent getForLocalItem(ItemId itemId)
	{
		return findByCriteria(Restrictions.eq("institution", CurrentInstitution.get()),
			Restrictions.eq("itemUuid", itemId.getUuid()), Restrictions.eq("itemVersion", itemId.getVersion()));
	}

	@Override
	@Transactional
	public PurchasedContent getForSourceUuid(final Store store, final String sourceUuid)
	{
		List<PurchasedContent> content = enumerateAll(new ListCallback()
		{
			@Override
			public String getAdditionalJoins()
			{
				return null;
			}

			@Override
			public String getAdditionalWhere()
			{
				return " sourceItemUuid = :sourceItemUuid AND store = :store ";
			}

			@Override
			public String getOrderBy()
			{
				return " ORDER BY be.sourceItemVersion DESC ";
			}

			@Override
			public void processQuery(Query query)
			{
				query.setString("sourceItemUuid", sourceUuid);
				query.setParameter("store", store);
			}

			@Override
			public boolean isDistinct()
			{
				return false;
			}
		});

		if( content != null && content.size() > 0 )
		{
			// The first is always the latest
			return content.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<String> getPurchased(final Set<String> uuids)
	{
		return Sets.newHashSet(getHibernateTemplate().executeFind(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				final Query query = session
					.createQuery("SELECT itemUuid FROM PurchasedContent WHERE itemUuid IN (:uuids)");
				query.setParameterList("uuids", uuids);
				return query.list();
			}
		}));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PurchasedContent> getLivePurchases(final Store store)
	{
		// The formatter keeps appending blank lines (more and more of them)
		//@formatter:off
		List<Object[]> result = getHibernateTemplate().executeFind(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				final Query query = session
					.createQuery("FROM PurchasedContent pc, Item i " +
							"WHERE pc.itemUuid = i.uuid " +
							"AND pc.itemVersion = i.version " +
							"AND i.status IN ('LIVE','REVIEW') " +
							"AND pc.store = :store ");
				query.setParameter("store", store);
				return query.list();
			}
		});
		//@formatter:on
		return Lists.newArrayList(Lists.transform(result, new Function<Object[], PurchasedContent>()
		{
			@Override
			public PurchasedContent apply(Object[] pair)
			{
				return (PurchasedContent) pair[0];
			}
		}));
	}

	private static class BuyerCallback implements ListCallback
	{
		private final String buyerUuid;

		protected BuyerCallback(String buyerUuid)
		{
			this.buyerUuid = buyerUuid;
		}

		@Override
		public String getAdditionalJoins()
		{
			return null;
		}

		@Override
		public String getAdditionalWhere()
		{
			return "be.checkoutBy = :checkoutBy ";
		}

		@Override
		public String getOrderBy()
		{
			// return "ORDER BY be.submitDate DESC";
			return null;
		}

		@Override
		public void processQuery(Query query)
		{
			query.setParameter("checkoutBy", buyerUuid);
		}

		@Override
		public boolean isDistinct()
		{
			return false;
		}
	}

}
