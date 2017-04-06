package com.tle.core.payment.dao.impl;

import java.util.Date;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.tle.common.payment.entity.SaleItem;
import com.tle.common.payment.entity.StoreFront;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.payment.dao.SaleItemDao;
import com.tle.core.user.CurrentInstitution;

@Bind(SaleItemDao.class)
@Singleton
public class SaleItemDaoImpl extends GenericDaoImpl<SaleItem, Long> implements SaleItemDao
{
	public SaleItemDaoImpl()
	{
		super(SaleItem.class);
	}

	@Override
	@SuppressWarnings({"unchecked", "nls"})
	public List<SaleItem> findSaleItemByItemUuid(final StoreFront storeFront, final String uuid,
		final boolean mustBePaidFor)
	{
		return (List<SaleItem>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public List<SaleItem> doInHibernate(Session session)
			{
				String queryString = "from SaleItem si where si.itemUuid = :itemUuid "
					+ "AND si.sale.institution = :insti ";
				if( storeFront != null )
				{
					queryString += " AND si.sale.storefront = :storefront ";
				}
				if( mustBePaidFor )
				{
					queryString += " AND si.sale.paidDate IS NOT NULL ";
				}
				Query query = session.createQuery(queryString);
				query.setParameter("itemUuid", uuid);
				if( storeFront != null )
				{
					query.setParameter("storefront", storeFront);
				}
				query.setParameter("insti", CurrentInstitution.get());
				return query.list();
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SaleItem> getPaidForSaleItems(final StoreFront storeFront, final Date referenceDate,
		final boolean onlyNewFlag, final Date createdSince, final int offset, final int max)
	{
		return getHibernateTemplate().executeFind(new HibernateCallback()
		{
			@Override
			public List<SaleItem> doInHibernate(Session session)
			{
				Query query = buildPaidForQuery(session, storeFront, referenceDate, onlyNewFlag, createdSince);

				if( offset >= 0 )
				{
					query.setFirstResult(offset);
				}
				if( max >= 0 )
				{
					query.setFetchSize(max);
					query.setMaxResults(max);
				}
				return query.list();
			}
		});
	}

	@Override
	public int countPaidForSaleItems(final StoreFront storeFront, final Date referenceDate, final boolean onlyNewFlag,
		final Date createdSince)
	{
		return (Integer) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Integer doInHibernate(Session session)
			{
				Query query = buildPaidForQuery(session, storeFront, referenceDate, onlyNewFlag, createdSince);
				return query.list().size();
			}
		});
	}

	@SuppressWarnings("nls")
	private Query buildPaidForQuery(Session session, StoreFront storeFront, Date referenceDate, boolean onlyNewFlag,
		final Date createdSince)
	{
		// @formatter:off
		String queryString = "from SaleItem si where "
			+ " si.sale.storefront = :storefront AND si.sale.institution = :insti "
			+ " AND si.sale.paidDate IS NOT NULL " 
			+ "           AND (si.subscriptionStartDate IS NULL "
			+ "				OR (si.subscriptionStartDate <= :referenceDate "
			+ "              AND si.subscriptionEndDate >= :referenceDate "
			+ "		) )";
		if( onlyNewFlag )
		{
			queryString += "\n AND not exists ( from StoreHarvestInfo starvin where starvin.sale = si.sale and starvin.itemUuid = si.itemUuid ) ";
		}

		// Must needs repeat test for createdSince != null, because the parameter must be set where required
		if( createdSince != null )
		{
			queryString += "\n AND exists ( from Item item where item.uuid = si.itemUuid and item.dateCreated >= :createdSince ) ";
		}
		// @formatter:on
		Query query = session.createQuery(queryString);

		query.setParameter("referenceDate", referenceDate);
		query.setParameter("storefront", storeFront);
		query.setParameter("insti", CurrentInstitution.get());
		if( createdSince != null )
		{
			query.setParameter("createdSince", createdSince);
		}

		return query;
	}

	@Override
	public int countSubscriptionsActiveWithinPeriod(final StoreFront storeFront, final Date startOfPeriod,
		final Date endOfPeriod)
	{
		return (Integer) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Integer doInHibernate(Session session)
			{
				Query query = buildActiveWithinQuery(session, storeFront, startOfPeriod, endOfPeriod);
				return query.list().size();
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SaleItem> getSubscriptionsActiveWithinPeriod(final StoreFront storeFront, final Date startOfPeriod,
		final Date endOfPeriod, final int offset, final int max)
	{
		return getHibernateTemplate().executeFind(new HibernateCallback()
		{
			@Override
			public List<SaleItem> doInHibernate(Session session)
			{
				Query query = buildActiveWithinQuery(session, storeFront, startOfPeriod, endOfPeriod);

				if( offset >= 0 )
				{
					query.setFirstResult(offset);
				}
				if( max >= 0 )
				{
					query.setFetchSize(max);
					query.setMaxResults(max);
				}
				return query.list();
			}
		});
	}

	/**
	 * A subscription is active (at some time at least) if its end date is
	 * greater than the period's start, and its start date is before the
	 * period's end.
	 * 
	 * @param session
	 * @param storeFront
	 * @param startOfPeriod
	 * @param endOfPeriod
	 * @return
	 */
	private Query buildActiveWithinQuery(Session session, StoreFront storeFront, Date startOfPeriod, Date endOfPeriod)
	{
		// @formatter:off
		String queryString = "from SaleItem si where "
			+ " si.sale.storefront = :storefront AND si.sale.institution = :insti "
			+ " AND si.subscriptionStartDate IS NOT NULL "
			+ " AND si.subscriptionEndDate >= :startOfPeriod "
			+ " AND si.subscriptionStartDate <= :endOfPeriod "
			+ " AND si.sale.paidDate IS NOT NULL";
		// @formatter:on
		Query query = session.createQuery(queryString);
		query.setParameter("storefront", storeFront);
		query.setParameter("insti", CurrentInstitution.get());
		query.setParameter("startOfPeriod", startOfPeriod);
		query.setParameter("endOfPeriod", endOfPeriod);
		return query;
	}

	@Override
	public int countSubscriptionsExpiringWithinPeriod(final StoreFront storeFront, final Date startOfPeriod,
		final Date endOfPeriod)
	{
		return (Integer) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Integer doInHibernate(Session session)
			{
				Query query = buildExpiringWithinQuery(session, storeFront, startOfPeriod, endOfPeriod);
				return query.list().size();
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SaleItem> getSubscriptionsExpiringWithinPeriod(final StoreFront storeFront, final Date startOfPeriod,
		final Date endOfPeriod, final int offset, final int max)
	{
		return getHibernateTemplate().executeFind(new HibernateCallback()
		{
			@Override
			public List<SaleItem> doInHibernate(Session session)
			{
				Query query = buildExpiringWithinQuery(session, storeFront, startOfPeriod, endOfPeriod);

				if( offset >= 0 )
				{
					query.setFirstResult(offset);
				}
				if( max >= 0 )
				{
					query.setFetchSize(max);
					query.setMaxResults(max);
				}
				return query.list();
			}
		});
	}

	/**
	 * A subscription expires within a period if its end date is greater than
	 * the period's start, and before the period's end.
	 * 
	 * @param session
	 * @param storeFront
	 * @param startOfPeriod
	 * @param endOfPeriod
	 * @return
	 */
	private Query buildExpiringWithinQuery(Session session, StoreFront storeFront, Date startOfPeriod, Date endOfPeriod)
	{
		// @formatter:off
		String queryString = "from SaleItem si where "
			+ " si.sale.storefront = :storefront AND si.sale.institution = :insti "
			+ " AND si.subscriptionStartDate IS NOT NULL "
			+ " AND si.subscriptionEndDate >= :startOfPeriod "
			+ " AND si.subscriptionEndDate <= :endOfPeriod "
			+ " AND si.sale.paidDate IS NOT NULL";
		// @formatter:on
		Query query = session.createQuery(queryString);
		query.setParameter("storefront", storeFront);
		query.setParameter("insti", CurrentInstitution.get());
		query.setParameter("startOfPeriod", startOfPeriod);
		query.setParameter("endOfPeriod", endOfPeriod);
		return query;
	}

	@Override
	@SuppressWarnings({"unchecked", "nls"})
	public List<SaleItem> getSalesItemsForSourceItem(final String itemUuid)
	{
		return (List<SaleItem>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public List<SaleItem> doInHibernate(Session session)
			{
				String queryString = "from SaleItem si where si.itemUuid = :itemUuid "
					+ "AND si.sale.institution = :insti AND si.sale.paidDate IS NOT NULL";
				Query query = session.createQuery(queryString);
				query.setParameter("itemUuid", itemUuid);
				query.setParameter("insti", CurrentInstitution.get());
				return query.list();
			}
		});
	}
}
