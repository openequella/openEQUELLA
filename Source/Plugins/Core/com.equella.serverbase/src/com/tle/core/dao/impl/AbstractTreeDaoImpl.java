/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.institution.CurrentInstitution;
import com.tle.common.institution.TreeNodeInterface;
import com.tle.core.dao.AbstractTreeDao;
import com.tle.core.hibernate.dao.GenericDaoImpl;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class AbstractTreeDaoImpl<T extends TreeNodeInterface<T>> extends GenericDaoImpl<T, Long>
	implements
		AbstractTreeDao<T>
{
	public AbstractTreeDaoImpl(Class<T> persistentClass)
	{
		super(persistentClass);
	}

	@Override
	public Long save(T entity)
	{
		setupParents(entity);
		return super.save(entity);
	}

	@Override
	public void saveOrUpdate(T entity)
	{
		setupParents(entity);
		super.saveOrUpdate(entity);
		updateChildrenParents(entity);
	}

	@Override
	public void update(T entity)
	{
		setupParents(entity);
		super.update(entity);
		updateChildrenParents(entity);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void delete(T node, AbstractTreeDao.DeleteAction<T> action)
	{
		deleteChildren(node, action);

		if( action != null )
		{
			action.beforeDelete(node);
		}

		super.delete(node);
	}

	@Override
	public void delete(T entity)
	{
		this.delete(entity, null);
	}

	@Override
	public void updateWithNoParentChange(T entity)
	{
		super.saveOrUpdate(entity);
	}

	protected void deleteChildren(T entity, AbstractTreeDao.DeleteAction<T> action)
	{
		for( T child : getChildrenForNode(entity, null) )
		{
			this.delete(child);
		}
	}

	protected void updateChildrenParents(T entity)
	{
		for( T child : getChildrenForNode(entity, null) )
		{
			update(child);
		}
	}

	protected void setupParents(T node)
	{
		List<T> allParents = Collections.emptyList();

		T parent = node.getParent();
		if( parent != null )
		{
			// Find by ID to make sure it's attached to the session
			parent = findById(parent.getId());

			allParents = new ArrayList<T>();
			if( parent.getAllParents() != null )
			{
				allParents.addAll(parent.getAllParents());
			}
			allParents.add(node.getParent());
		}

		node.setAllParents(allParents);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<T> getRootNodes(String orderBy)
	{
		StringBuilder query = new StringBuilder("from ");
		query.append(getPersistentClass().getName());
		query.append(" where parent is null and institution = :institution");
		if( orderBy != null )
		{
			query.append(" order by ");
			query.append(orderBy);
		}

		return getHibernateTemplate().findByNamedParam(query.toString(), "institution", CurrentInstitution.get());
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<T> getChildrenForNode(T node, String orderBy)
	{
		StringBuilder query = new StringBuilder("from ");
		query.append(getPersistentClass().getName());
		query.append(" where parent = :node");

		if( orderBy != null )
		{
			query.append(" order by ");
			query.append(orderBy);
		}

		return getHibernateTemplate().findByNamedParam(query.toString(), "node", node);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Long> getChildrenIdsForNodeId(long nodeId, String orderBy)
	{
		StringBuilder query = new StringBuilder("select id from ");
		query.append(getPersistentClass().getName());
		query.append(" where parent.id = :nodeId");

		if( orderBy != null )
		{
			query.append(" order by ");
			query.append(orderBy);
		}

		return getHibernateTemplate().findByNamedParam(query.toString(), "nodeId", nodeId);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<T> getAllSubnodeForNode(T node)
	{
		return getHibernateTemplate().findByNamedParam(
			"from " + getPersistentClass().getName() + " where :node in elements(allParents)", "node", node);
	}

	@Override
	public int countRootNodes()
	{
		return ((Long) getHibernateTemplate()
			.findByNamedParam("SELECT COUNT(*) FROM " + getPersistentClass().getName()
				+ " WHERE  parent IS NULL AND institution = :institution", "institution", CurrentInstitution.get())
			.get(0)).intValue();
	}

	@Override
	public int countSubnodesForNode(T node)
	{
		return ((Long) getHibernateTemplate().findByNamedParam(
			"SELECT COUNT(*) FROM " + getPersistentClass().getName() + " WHERE :node IN ELEMENTS(allParents)", "node",
			node).get(0)).intValue();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<T> listAll()
	{
		return getHibernateTemplate().find("from " + getPersistentClass().getName() + " where institution = ?",
			CurrentInstitution.get());
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteInOrder(Long node)
	{
		T t = findById(node);
		delete(t);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Long> listIdsInOrder()
	{
		return getHibernateTemplate().executeFind(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				Query query = session.createQuery("SELECT id FROM " //$NON-NLS-1$
					+ getPersistentClass().getName()
					+ " WHERE institution = :institution ORDER BY allParents.size ASC"); //$NON-NLS-1$
				query.setParameter("institution", CurrentInstitution.get()); //$NON-NLS-1$
				query.setCacheable(true);
				query.setReadOnly(true);

				return query.list();
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Long> enumerateIdsInOrder()
	{
		return getHibernateTemplate().executeFind(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				// NOTE: Don't order by name here - use NumberStringComparator
				// on the returned list.
				Query query = session.createQuery("select id, parent.id from " + getPersistentClass().getName() //$NON-NLS-1$
					+ " where institution = :institution"); //$NON-NLS-1$
				query.setParameter("institution", CurrentInstitution.get()); //$NON-NLS-1$
				query.setCacheable(true);
				query.setReadOnly(true);
				Map<Long, List<Long>> allNodesMap = new HashMap<Long, List<Long>>();
				List<Object[]> list = query.list();
				for( Object[] obj : list )
				{
					Long id = (Long) obj[0];
					Long parentId = (Long) obj[1];
					List<Long> children = allNodesMap.get(parentId);
					if( children == null )
					{
						children = new ArrayList<Long>();
						allNodesMap.put(parentId, children);
					}
					children.add(id);
				}
				return addIdsToList(new ArrayList<Long>(), allNodesMap.get(null), allNodesMap);
			}

			private Object addIdsToList(List<Long> outIds, List<Long> children, Map<Long, List<Long>> allNodesMap)
			{
				if( children == null )
				{
					return outIds;
				}
				for( Long id : children )
				{
					addIdsToList(outIds, allNodesMap.get(id), allNodesMap);
					outIds.add(id);
				}
				return outIds;
			}
		});
	}
}
