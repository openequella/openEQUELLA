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

package com.tle.core.hierarchy.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;
import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.hierarchy.HierarchyTopicDynamicKeyResources;
import com.tle.beans.item.Item;
import com.tle.core.dao.impl.AbstractTreeDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyDao;
import com.tle.core.i18n.dao.LanguageDao;
import com.tle.common.institution.CurrentInstitution;

/**
 * @author Nicholas Read
 */
@Bind(HierarchyDao.class)
@Singleton
@SuppressWarnings("nls")
public class HierarchyDaoImpl extends AbstractTreeDaoImpl<HierarchyTopic> implements HierarchyDao
{
	@Inject
	private LanguageDao languageDao;

	public HierarchyDaoImpl()
	{
		super(HierarchyTopic.class);
	}

	@Override
	public LanguageBundle getHierarchyTopicName(final long topicID)
	{
		return (LanguageBundle) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				Query query = session.createQuery("select name from HierarchyTopic where id = :id");
				query.setParameter("id", topicID);
				query.setCacheable(true);

				Iterator<?> iter = query.iterate();
				if( iter.hasNext() )
				{
					return iter.next();
				}
				else
				{
					return null;
				}
			}
		});
	}

	@Override
	@Transactional
	public void deleteInOrder(Long node)
	{
		HierarchyTopic topic = findById(node);
		List<Long> bundleIds = new ArrayList<Long>();
		addBundleId(topic.getName(), bundleIds);
		addBundleId(topic.getShortDescription(), bundleIds);
		addBundleId(topic.getLongDescription(), bundleIds);
		addBundleId(topic.getSubtopicsSectionName(), bundleIds);
		addBundleId(topic.getResultsSectionName(), bundleIds);
		delete(topic);
		flush();
		languageDao.deleteBundles(bundleIds);
	}

	private void addBundleId(LanguageBundle bundle, List<Long> bundleIds)
	{
		if( bundle != null )
		{
			bundleIds.add(bundle.getId());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.dao.HierarchyDao#enumerateAll()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<HierarchyTopic> enumerateAll()
	{
		return getHibernateTemplate().find("from HierarchyTopic where institution = ?", CurrentInstitution.get());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.dao.HierarchyDao#getTopicsReferencingItemDefinition(com.
	 * tle.beans.entity.itemdef.ItemDefinition)
	 */
	@Override
	public Collection<HierarchyTopic> getTopicsReferencingItemDefinition(ItemDefinition itemDefinition)
	{
		return getTopicsReferencingEntity(itemDefinition, "Idefs");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.dao.HierarchyDao#getTopicsReferencingSchema(com.tle.beans
	 * .entity.Schema)
	 */
	@Override
	public Collection<HierarchyTopic> getTopicsReferencingSchema(Schema schema)
	{
		return getTopicsReferencingEntity(schema, "Schemas");
	}

	@SuppressWarnings("unchecked")
	private Collection<HierarchyTopic> getTopicsReferencingEntity(BaseEntity entity, String entityPostfix)
	{
		List list1 = getHibernateTemplate().findByNamedParam(
			"from HierarchyTopic t join t.inh" + entityPostfix + " a where a.entity = :entity", "entity", entity);
		List list2 = getHibernateTemplate().findByNamedParam(
			"from HierarchyTopic t join t.add" + entityPostfix + " a where a.entity = :entity", "entity", entity);
		HashSet<HierarchyTopic> topics = new HashSet<HierarchyTopic>();
		topics.addAll(list1);
		topics.addAll(list2);
		return topics;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.dao.HierarchyDao#getTopicsReferencingPowerSearch(com.tle
	 * .beans.entity.PowerSearch)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<HierarchyTopic> getTopicsReferencingPowerSearch(PowerSearch powerSearch)
	{
		return getHibernateTemplate().findByNamedParam("from HierarchyTopic t where advancedSearch = :powerSearch",
			"powerSearch", powerSearch);
	}

	@Override
	@SuppressWarnings("unchecked")
	public HierarchyTopic findByUuid(String uuid, Institution institution)
	{
		Preconditions.checkNotNull(uuid);
		List<HierarchyTopic> topics = getHibernateTemplate().find(
			"from HierarchyTopic t WHERE uuid = ? AND institution = ?", new Object[]{uuid, institution});
		if( topics.size() == 0 )
		{
			return null;
		}
		return topics.get(0);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void removeReferencesToItem(Item item, long id)
	{
		List<HierarchyTopic> topics = getHibernateTemplate().find(
			"from HierarchyTopic t WHERE ? in elements(keyResources) AND t.id = ?", new Object[]{item, id});
		for( HierarchyTopic topic : topics )
		{
			List<Item> keyResources = topic.getKeyResources();
			Iterator<Item> iter = keyResources.iterator();
			while( iter.hasNext() )
			{
				Item anItem = iter.next();
				if( anItem.getId() == item.getId() )
				{
					iter.remove();
				}
			}
			getHibernateTemplate().save(topic);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void removeReferencesToItem(Item item)
	{
		List<HierarchyTopic> topics = getHibernateTemplate().find(
			"from HierarchyTopic t WHERE ? in elements(keyResources)", item);
		for( HierarchyTopic topic : topics )
		{
			List<Item> keyResources = topic.getKeyResources();
			Iterator<Item> iter = keyResources.iterator();
			while( iter.hasNext() )
			{
				Item anItem = iter.next();
				if( anItem.getId() == item.getId() )
				{
					iter.remove();
				}
			}
			getHibernateTemplate().save(topic);
		}
	}

	@Override
	public void removeDynamicKeyResource(String itemUuid, int itemVersion, Institution institution)
	{
		getHibernateTemplate().bulkUpdate(
			"delete from HierarchyTopicDynamicKeyResources WHERE uuid = ? AND version =? " + "AND institution = ?",
			new Object[]{itemUuid, itemVersion, institution});
	}

	@Override
	public void removeDynamicKeyResource(String topicId, String itemUuid, int itemVersion)
	{
		getHibernateTemplate().bulkUpdate(
			"delete from HierarchyTopicDynamicKeyResources WHERE "
				+ "dynamicHierarchyId = ? AND uuid = ? AND version =?", new Object[]{topicId, itemUuid, itemVersion});
	}

	@Override
	public void deleteAllDynamicKeyResources(Institution institution)
	{
		List<HierarchyTopicDynamicKeyResources> keyResources = getAllDynamicKeyResources(institution);
		for( HierarchyTopicDynamicKeyResources key : keyResources )
		{
			getHibernateTemplate().delete(key);
		}
	}

	@Override
	public void saveDynamicKeyResources(HierarchyTopicDynamicKeyResources entity)
	{
		getHibernateTemplate().save(entity);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<HierarchyTopicDynamicKeyResources> getDynamicKeyResource(String dynamicHierarchyId,
		Institution institution)
	{
		List<HierarchyTopicDynamicKeyResources> dynamicKeyResources = getHibernateTemplate().find(
			"from HierarchyTopicDynamicKeyResources t WHERE dynamicHierarchyId = ? AND institution = ?",
			new Object[]{dynamicHierarchyId, institution});
		if( dynamicKeyResources.size() == 0 )
		{
			return null;
		}
		return dynamicKeyResources;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<HierarchyTopicDynamicKeyResources> getDynamicKeyResource(String dynamicHierarchyId, String itemUuid,
		int itemVersion, Institution institution)
	{
		List<HierarchyTopicDynamicKeyResources> dynamicKeyResources = getHibernateTemplate().find(
			"from HierarchyTopicDynamicKeyResources t WHERE dynamicHierarchyId = ? AND uuid = ? "
				+ "AND version =? AND institution = ?",
			new Object[]{dynamicHierarchyId, itemUuid, itemVersion, institution});
		if( dynamicKeyResources.size() == 0 )
		{
			return null;
		}
		return dynamicKeyResources;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<HierarchyTopicDynamicKeyResources> getDynamicKeyResource(String itemUuid, int itemVersion,
		Institution institution)
	{

		List<HierarchyTopicDynamicKeyResources> dynamicKeyResources = getHibernateTemplate().find(
			"from HierarchyTopicDynamicKeyResources t WHERE uuid = ? AND version =? AND  institution = ?",
			new Object[]{itemUuid, itemVersion, institution});
		if( dynamicKeyResources.size() == 0 )
		{
			return null;
		}
		return dynamicKeyResources;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<HierarchyTopicDynamicKeyResources> getAllDynamicKeyResources(Institution institution)
	{
		List<HierarchyTopicDynamicKeyResources> dynamicKeyResources = getHibernateTemplate().find(
			"from HierarchyTopicDynamicKeyResources t WHERE institution =?", new Object[]{institution});
		return dynamicKeyResources;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<HierarchyTopic> findKeyResource(Item item)
	{
		List<HierarchyTopic> topics = getHibernateTemplate().find(
			"from HierarchyTopic t WHERE ? in elements(keyResources)", item);
		return topics;
	}
}
