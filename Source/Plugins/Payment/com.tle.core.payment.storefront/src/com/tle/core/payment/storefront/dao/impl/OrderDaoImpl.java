package com.tle.core.payment.storefront.dao.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.tle.common.payment.storefront.entity.Order;
import com.tle.common.payment.storefront.entity.Order.Status;
import com.tle.common.payment.storefront.entity.OrderHistory;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.payment.storefront.dao.OrderDao;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind(OrderDao.class)
@Singleton
public class OrderDaoImpl extends GenericInstitionalDaoImpl<Order, Long> implements OrderDao
{
	public OrderDaoImpl()
	{
		super(Order.class);
	}

	@Override
	public Order getShoppingCart()
	{
		return findByCriteria(Restrictions.eq("institution", CurrentInstitution.get()),
			Restrictions.eq("createdBy", CurrentUser.getUserID()), Restrictions.eq("status", Status.CART.ordinal()));
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void deleteAll()
	{
		for( Order order : enumerateAll() )
		{
			delete(order);
		}
	}

	@Override
	public Order getByUuid(String uuid)
	{
		return findByCriteria(Restrictions.eq("institution", CurrentInstitution.get()), Restrictions.eq("uuid", uuid));
	}

	@Override
	public List<Order> getByUuidsWithStatus(List<String> uuids, Order.Status status)
	{
		Criterion[] cs = {Restrictions.eq("institution", CurrentInstitution.get()), Restrictions.in("uuid", uuids),
				Restrictions.eq("status", status.ordinal())};
		return findAllByCriteria(org.hibernate.criterion.Order.desc("lastActionDate"), MAX_CURSORY_ENQUIRY, cs);
	}

	@Override
	public List<Order> enumerateSent(final String userId)
	{
		return enumerateAll(new StatusCallback(true, Status.COMPLETE, Status.CART)
		{
			@Override
			public String getAdditionalWhere()
			{
				return super.getAdditionalWhere() + "AND be.createdBy = :createdBy ";
			}

			@Override
			public void processQuery(Query query)
			{
				super.processQuery(query);
				query.setParameter("createdBy", userId);
			}
		});
	}

	@Override
	public List<Order> enumerateApproval()
	{
		return enumerateAll(new StatusCallback(Status.APPROVAL));
	}

	@Override
	public List<Order> enumeratePayment()
	{
		return enumerateAll(new StatusCallback(Status.PAYMENT));
	}

	@Override
	public List<Order> enumeratePaymentAndPending()
	{
		return enumerateAll(new StatusCallback(Status.PAYMENT, Status.PENDING));
	}

	@Override
	public List<String> getUnfinishedOrderUuids()
	{
		List<Integer> statuses = Lists.newArrayList(Status.PAYMENT.ordinal(), Status.PENDING.ordinal());

		String hql = "select o.uuid from Order o where o.institution = :institution and o.status in :statuses";
		return getHibernateTemplate().findByNamedParam(hql, new String[]{"institution", "statuses"},
			new Object[]{CurrentInstitution.get(), statuses});

	}

	private static class StatusCallback implements ListCallback
	{
		private final Status[] statuses;
		private final boolean not;

		protected StatusCallback(Status... statuses)
		{
			this.statuses = statuses;
			this.not = false;
		}

		protected StatusCallback(boolean not, Status... statuses)
		{
			this.statuses = statuses;
			this.not = not;
		}

		@Override
		public String getAdditionalJoins()
		{
			return null;
		}

		@Override
		public String getAdditionalWhere()
		{
			if( not )
			{
				return "be.status not in (:statuses) ";
			}
			return "be.status in (:statuses) ";
		}

		@Override
		public String getOrderBy()
		{
			return "ORDER BY be.lastActionDate DESC";
		}

		@Override
		public void processQuery(Query query)
		{
			final Set<Integer> ords = new HashSet<Integer>(statuses.length);
			for( int i = 0; i < statuses.length; i++ )
			{
				ords.add(statuses[i].ordinal());
			}
			query.setParameterList("statuses", ords);
		}

		@Override
		public boolean isDistinct()
		{
			return false;
		}
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void deleteAllForCreator(final String userId)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query q = session.createQuery("FROM Order WHERE createdBy = :createdBy AND institution = :institution");
				q.setParameter("createdBy", userId);
				q.setParameter("institution", CurrentInstitution.get());
				for( Object b : q.list() )
				{
					session.delete(b);
				}
				return null;
			}
		});
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void updateCreator(final String fromUserId, final String toUserId)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query q = session.createQuery("UPDATE Order SET createdBy = :toUser WHERE createdBy = :fromUser"
					+ " AND institution = :institution");
				q.setParameter("fromUser", fromUserId);
				q.setParameter("toUser", toUserId);
				q.setParameter("institution", CurrentInstitution.get());
				q.executeUpdate();
				return null;
			}
		});
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void updateLastActionUser(final String fromUserId, final String toUserId)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query q = session.createQuery("UPDATE Order SET lastActionUser = :toUser WHERE createdBy = :fromUser"
					+ " AND institution = :institution");
				q.setParameter("fromUser", fromUserId);
				q.setParameter("toUser", toUserId);
				q.setParameter("institution", CurrentInstitution.get());
				q.executeUpdate();
				return null;
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public List<OrderHistory> findHistoryForUser(final String userId)
	{
		return getHibernateTemplate().executeFind(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query q = session
					.createQuery("FROM OrderHistory WHERE userId = :userId AND order.institution = :institution");
				q.setParameter("userId", userId);
				q.setParameter("institution", CurrentInstitution.get());
				return q.list();
			}
		});
	}
}
