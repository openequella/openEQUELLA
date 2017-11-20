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

package com.tle.core.copyright.dao;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.Section;
import com.tle.core.hibernate.dao.AbstractHibernateDao;
import com.tle.core.item.dao.ItemDaoExtension;

@SuppressWarnings({"unchecked", "nls"})
public abstract class AbstractCopyrightDao<H extends Holding, P extends Portion, S extends Section>
	extends
		AbstractHibernateDao
	implements CopyrightDao<H, P, S>, ItemDaoExtension
{
	private Map<String, String> queries = Maps.newIdentityHashMap();

	protected abstract String getSectionEntity();

	protected abstract String getPortionEntity();

	protected abstract String getHoldingEntity();

	protected abstract String getPortionTable();

	protected abstract String getHoldingTable();

	private synchronized String query(String query)
	{
		String newQuery = queries.get(query);
		if( newQuery != null )
		{
			return newQuery;
		}
		newQuery = query.replace("%h", getHoldingEntity());
		newQuery = newQuery.replace("%p", getPortionEntity());
		newQuery = newQuery.replace("%s", getSectionEntity());
		queries.put(query, newQuery);
		return newQuery;
	}

	private synchronized String sqlQuery(String query)
	{
		String newQuery = queries.get(query);
		if( newQuery != null )
		{
			return newQuery;
		}
		newQuery = query.replace("%h", getHoldingTable());
		newQuery = newQuery.replace("%p", getPortionTable());
		queries.put(query, newQuery);
		return newQuery;
	}

	@Override
	public void delete(Item item)
	{
		deleteAllForItem(item);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteAllForItem(final Item item)
	{
		final long itemId = item.getId();
		final HibernateTemplate hibernateTemplate = getHibernateTemplate();

		hibernateTemplate.execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				SQLQuery sqlQuery = session.createSQLQuery(sqlQuery(
					"select count(h.item_id) as hcount, count(p.item_id) as pcount from item i left outer join %h h on h.item_id = i.id "
						+ "left outer join %p p on p.item_id = i.id where i.id = ? and (h.id is not null or p.id is not null)"));
				sqlQuery.setLong(0, itemId);
				Object[] hasSome = (Object[]) sqlQuery.uniqueResult();
				int numHoldings = ((Number) hasSome[0]).intValue();
				int numPortions = ((Number) hasSome[1]).intValue();
				if( numPortions > 0 )
				{
					deletePortionExtras(session, itemId);
					hibernateTemplate.bulkUpdate(query("delete from %s where portion in (from %p where item.id = ?)"),
						itemId);
					hibernateTemplate.bulkUpdate(query("delete from %p where item.id = ?"), itemId);
				}
				if( numHoldings > 0 )
				{
					deleteHoldingExtras(session, itemId);
					hibernateTemplate.bulkUpdate(
						query("update %p set holding = null where holding in (from %h where item.id = ?)"), itemId);
					hibernateTemplate.bulkUpdate(query("delete from %h where item.id = ?"), itemId);
				}
				return null;
			}
		});
	}

	protected void deleteHoldingExtras(Session session, long itemId)
	{
		SQLQuery sqlQuery = session
			.createSQLQuery(sqlQuery("delete from %h_authors where %h_id in (select id from %h where item_id = ?)"));
		sqlQuery.setLong(0, itemId);
		sqlQuery.executeUpdate();
		sqlQuery = session
			.createSQLQuery(sqlQuery("delete from %h_ids where %h_id in (select id from %h where item_id = ?)"));
		sqlQuery.setLong(0, itemId);
		sqlQuery.executeUpdate();
	}

	protected void deletePortionExtras(Session session, long itemId)
	{
		SQLQuery sqlQuery = session
			.createSQLQuery(sqlQuery("delete from %p_authors where %p_id in (select id from %p where item_id = ?)"));
		sqlQuery.setLong(0, itemId);
		sqlQuery.executeUpdate();
		sqlQuery = session
			.createSQLQuery(sqlQuery("delete from %p_topics where %p_id in (select id from %p where item_id = ?)"));
		sqlQuery.setLong(0, itemId);
		sqlQuery.executeUpdate();
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public long save(Object entity)
	{
		return (Long) getHibernateTemplate().save(entity);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<P> getPortionsForItems(List<Item> items)
	{
		if( items.isEmpty() )
		{
			return Collections.emptyList();
		}
		return getHibernateTemplate().findByNamedParam(query("from %p p where p.item in (:items)"), "items", items);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public H getHoldingInItem(Item item)
	{
		List<H> holdings = getHibernateTemplate().find(query("from %h where item = ?"), item);
		if( !holdings.isEmpty() )
		{
			if( holdings.size() > 1 )
			{
				throw new RuntimeException("Too many holdings for item:" + item.getId());
			}
			return holdings.get(0);
		}
		return null;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public H getHoldingForItem(Item item)
	{
		H holding = getHoldingInItem(item);
		if( holding != null )
		{
			return holding;
		}
		List<H> holdings = getHibernateTemplate().find(query("select p.holding from %p p where p.item = ?"), item); //$NON-NLS-1$
		return holdings.isEmpty() ? null : holdings.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Item> getAllItemsForHolding(H holding)
	{
		// Dirty code for Oracle bug (cannot use "select distinct(p.item)")
		List<Item> items = getHibernateTemplate().find(query("select p.item from %p p where p.item.id in "
			+ "(select distinct(p2.item.id) from %p p2 where p2.holding = ?)"), holding);

		return items;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public S getSectionForAttachment(Item item, String attachmentUuid)
	{
		List<S> sections = getHibernateTemplate().findByNamedParam(
			query("from %s where portion.item = :item and attachment = :attachment"),
			new String[]{"item", "attachment"}, new Object[]{item, attachmentUuid});
		if( sections.isEmpty() )
		{
			return null;
		}
		return sections.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public long saveHolding(Item item, H holding)
	{
		holding.setItem(item);
		return save(holding);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void savePortions(Item item, H holding, List<P> portions)
	{
		for( P portion : portions )
		{
			portion.setItem(item);
			portion.setHolding(holding);
			save(portion);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateHoldingReference(H holding, List<Item> items)
	{
		List<P> portions = getPortionsForItems(items);
		if( holding != null )
		{
			holding.setPortions(portions);
		}
		for( P portion : portions )
		{
			portion.setHolding(holding);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Map<Long, H> getHoldingsForItems(List<Item> items)
	{
		Map<Long, H> holdingMap = new HashMap<Long, H>();
		if( !items.isEmpty() )
		{
			List<Object[]> holdingItems = getHibernateTemplate()
				.findByNamedParam(query("select p.holding, p.item from %p p where p.item in (:items)"), "items", items);
			for( Object[] objs : holdingItems )
			{
				holdingMap.put(((Item) objs[1]).getId(), (H) objs[0]);
			}
			holdingItems = getHibernateTemplate()
				.findByNamedParam(query("select h, h.item from %h h where h.item in (:items)"), "items", items);
			for( Object[] objs : holdingItems )
			{
				holdingMap.put(((Item) objs[1]).getId(), (H) objs[0]);
			}
		}
		return holdingMap;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Attachment getSectionAttachmentForFilepath(Item item, String filepath)
	{
		Iterator<Attachment> iter = getHibernateTemplate().iterate(
			query("select a from %s s join s.portion as p join p.item as i join i.attachments as a"
				+ " where a.class = FileAttachment and a.uuid = s.attachment and a.url = ? and i = ?"),
			new Object[]{filepath, item});
		if( !iter.hasNext() )
		{
			return null;
		}
		return iter.next();
	}

}
