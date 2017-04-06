package com.tle.core.payment.dao.impl;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.item.Item;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.CatalogueAssignment;
import com.tle.core.dao.ItemDaoExtension;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.payment.dao.CatalogueAssignmentDao;

@SuppressWarnings("nls")
@Bind(CatalogueAssignmentDao.class)
@Singleton
public class CatalogueAssignmentDaoImpl extends GenericDaoImpl<CatalogueAssignment, Long>
	implements
		CatalogueAssignmentDao,
		ItemDaoExtension
{
	public CatalogueAssignmentDaoImpl()
	{
		super(CatalogueAssignment.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CatalogueAssignment> enumerateForItem(final Item item)
	{
		return (List<CatalogueAssignment>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				return session.createQuery("FROM CatalogueAssignment WHERE item = :item").setParameter("item", item)
					.list();
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CatalogueAssignment> enumerateForItem(final Item item, final boolean blacklist)
	{
		return (List<CatalogueAssignment>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				return session
					.createQuery("FROM CatalogueAssignment WHERE item = :item AND blacklisted = :blacklisted")
					.setParameter("item", item).setParameter("blacklisted", blacklist).list();
			}
		});
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void delete(Item item)
	{
		String query = "delete from CatalogueAssignment where item = ?";
		getHibernateTemplate().bulkUpdate(query, item);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public boolean addToList(Catalogue catalogue, Item item, boolean blacklist)
	{
		boolean modified = false;
		List<CatalogueAssignment> list = getHibernateTemplate().find(
			"from CatalogueAssignment where item = ? and catalogue = ?", new Object[]{item, catalogue});

		CatalogueAssignment assignment;
		if( list == null || list.isEmpty() )
		{
			modified = true;
			assignment = new CatalogueAssignment();
			assignment.setItem(item);
			assignment.setCatalogue(catalogue);
		}
		else
		{
			assignment = list.get(0);
			modified = (blacklist != assignment.isBlacklisted());
		}
		assignment.setBlacklisted(blacklist);

		if( modified )
		{
			saveOrUpdate(assignment);
		}

		return modified;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public boolean removeFromList(Catalogue catalogue, Item item)
	{
		String query = "delete from CatalogueAssignment where item = ? and catalogue = ?";
		return getHibernateTemplate().bulkUpdate(query, new Object[]{item, catalogue}) > 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CatalogueAssignment> enumerateForCatalogue(final Catalogue catalogue)
	{
		return (List<CatalogueAssignment>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				return session.createQuery("FROM CatalogueAssignment WHERE catalogue = :catalogue")
					.setParameter("catalogue", catalogue).list();
			}
		});
	}
}
