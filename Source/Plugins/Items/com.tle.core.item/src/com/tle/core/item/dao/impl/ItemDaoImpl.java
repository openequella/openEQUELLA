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

package com.tle.core.item.dao.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.valuebean.ItemIndexDate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.IdCloneable;
import com.tle.beans.Institution;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.DrmAcceptance;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemSelect;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.Triple;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.dao.helpers.CollectionPartitioner;
import com.tle.core.dao.helpers.DMLPartitioner;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.item.dao.ItemDao;
import com.tle.core.item.dao.ItemDaoExtension;
import com.tle.core.item.dao.ItemLockingDao;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;

/**
 * @author Nicholas Read
 */
@NonNullByDefault
@Bind(ItemDao.class)
@Singleton
@SuppressWarnings({"nls", "unchecked"})
public class ItemDaoImpl extends GenericInstitionalDaoImpl<Item, Long> implements ItemDao
{
	@Inject
	private ItemLockingDao itemLockingDao;

	private PluginTracker<ItemDaoExtension> itemDaoTracker;

	public ItemDaoImpl()
	{
		super(Item.class);
	}

	@Override
	public Long save(Item entity)
	{
		sanityCheck(entity);
		return super.save(entity);
	}

	@Override
	public void saveOrUpdate(Item entity)
	{
		sanityCheck(entity);
		super.saveOrUpdate(entity);
	}

	private void sanityCheck(Item item)
	{
		long itemInstId = item.getInstitution().getUniqueId();
		long itemDefInstId = item.getItemDefinition().getInstitution().getUniqueId();
		if( itemInstId != itemDefInstId )
		{
			throw new RuntimeException("Illegal attempt to save an item in an instituion (" + itemInstId
				+ ") that differs from the institution of the collection (" + itemDefInstId
				+ ").  Please contact your system administrator.");
		}
	}

	/**
	 * Return a list of items for a list of keys. The returned items MUST be in
	 * the same order as the original list of keys, thus nulls should be
	 * inserted for missing items.
	 */
	@Override
	public List<Item> getItems(final List<Long> keys, final ItemSelect select, final Institution institution)
	{
		if( keys.isEmpty() )
		{
			return Collections.emptyList();
		}

		List<Item> itemList = load(select, keys, institution, true);

		Map<Long, Item> itemMap = new HashMap<Long, Item>(itemList.size());
		for( Item item : itemList )
		{
			itemMap.put(item.getId(), item);
		}

		List<Item> results = new ArrayList<Item>(keys.size());
		for( Long key : keys )
		{
			// null's need to be inserted if the item doesn't exist
			results.add(itemMap.get(key));
		}
		return results;
	}

	@Override
	public Item get(final ItemKey id)
	{
		return get(id, true);
	}

	@Override
	public Item get(final ItemKey id, final boolean readOnly)
	{
		return findByItemId(id, readOnly);
	}

	private List<Item> load(final ItemSelect select, Collection<Long> keys, final Institution institution,
		final boolean readOnly)
	{
		StringBuilder sb = new StringBuilder("FROM Item item");
		if( select != null )
		{
			List<String> ones = select.listOneToOnes();
			for( String s : ones )
			{
				sb.append(" LEFT JOIN FETCH item.");
				sb.append(s);
			}
		}

		sb.append(" WHERE item.id IN (:key)");
		if( institution != null )
		{
			sb.append(" AND item.institution = :institution");
		}
		final String hql = sb.toString();

		return getHibernateTemplate().executeFind(new CollectionPartitioner<Long, Item>(keys)
		{
			@Override
			public List<Item> doQuery(Session session, Collection<Long> keys)
			{
				Query query = session.createQuery(hql);
				query.setReadOnly(readOnly);
				query.setParameterList("key", keys);
				if( institution != null )
				{
					query.setParameter("institution", institution);
				}

				List<Item> l = query.list();
				if( select != null && !l.isEmpty() )
				{
					select.initialise(l.get(0));
				}
				return l;
			}
		});
	}

	@Override
	public List<ItemId> getItemsWithUrl(String url, ItemDefinition itemDefinition, String excludedUuid)
	{
		List<String> names = new ArrayList<String>();
		List<Object> values = new ArrayList<Object>();

		StringBuilder query = new StringBuilder();
		query.append("SELECT new com.tle.beans.item.ItemId(i.uuid, i.version)");
		query.append(" FROM Item AS i INNER JOIN i.attachments AS a");

		query.append(" WHERE a.url = :url AND a.class = LinkAttachment AND i.status IN ('LIVE', 'REVIEW')");
		names.add("url");
		values.add(url);

		if( itemDefinition != null )
		{
			query.append(" AND i.itemDefinition = :itemDefinition");
			names.add("itemDefinition");
			values.add(itemDefinition);
		}

		if( excludedUuid != null )
		{
			query.append(" AND i.uuid != :excludedUuid");
			names.add("excludedUuid");
			values.add(excludedUuid);
		}

		List<ItemId> items = getHibernateTemplate().findByNamedParam(query.toString(),
			names.toArray(new String[names.size()]), values.toArray());

		return items;
	}

	@Override
	public List<Item> getNextLiveItems(List<ItemId> keys)
	{
		Map<String, Integer> map = new HashMap<String, Integer>();
		for( ItemId key : keys )
		{
			map.put(key.getUuid(), key.getVersion());
		}
		List<ItemIdKey> list = getHibernateTemplate().findByNamedParam(
			"select new com.tle.beans.item.ItemIdKey(id, uuid, max(version)) from Item "
				+ "where uuid in (:ids) group by id, uuid, version, status having status = :status order by version",
			new String[]{"ids", "status"}, new Object[]{map.keySet(), ItemStatus.LIVE.name()});
		List<Long> ids = new ArrayList<Long>();
		for( ItemIdKey key : list )
		{
			if( map.get(key.getUuid()) < key.getVersion() )
			{
				ids.add(key.getKey());
			}
		}
		return getItems(ids, new ItemSelect(), CurrentInstitution.get());
	}

	@Override
	@Transactional
	public Pair<Long, Long> getIdRange(Collection<Institution> institutions, Date afterDate)
	{
		List<?> minMax = getHibernateTemplate().findByNamedParam(
			"select min(i.id), max(i.id) from Item i where i.institution in (:insts) and dateForIndex >= :afterDate",
			new String[]{"insts", "afterDate"}, new Object[]{institutions, afterDate});
		Object[] minMaxArr = (Object[]) minMax.get(0);
		if( minMaxArr[0] == null )
		{
			return null;
		}
		return new Pair<Long, Long>(((Number) minMaxArr[0]).longValue(), ((Number) minMaxArr[1]).longValue());
	}

	@Override
	@Transactional
	public List<ItemIndexDate> getIndexTimesFromId(final Collection<Institution> institutions, final Date afterDate,
		final long itemId, final long maxItemId, final int maxResults)
	{
		if( institutions.isEmpty() )
		{
			return Collections.emptyList();
		}
		return getHibernateTemplate().executeFind(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session.createQuery("select new com.dytech.edge.common.valuebean.ItemIndexDate("
					+ "i.id, i.uuid, i.version, i.dateForIndex, i.institution.uniqueId) from Item i"
					+ " where i.institution in (:insts) and i.dateForIndex >= :afterDate and i.id >= :itemId and i.id <= :maxItemId order by i.id asc");
				query.setParameterList("insts", institutions);
				query.setParameter("afterDate", afterDate);
				query.setParameter("itemId", itemId);
				query.setParameter("maxItemId", maxItemId);
				query.setMaxResults(maxResults);
				return query.list();
			}
		});
	}

	@Override
	public List<ItemId> enumerateItemKeys(String whereClause, String[] names, Object[] values)
	{
		String hql = "select new com.tle.beans.item.ItemId(i.uuid, i.version) from Item i " + whereClause;
		return getHibernateTemplate().findByNamedParam(hql, names, values);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ItemIdKey> getItemKeyBatch(final String joinClause, final String whereClause,
		final Map<String, Object> params, final long startId, final int batchSize)
	{
		return getHibernateTemplate().executeFind(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query q = session.createQuery(
					"SELECT DISTINCT NEW com.tle.beans.item.ItemIdKey(i.id, i.uuid, i.version) FROM Item i "
						+ joinClause + " WHERE (" + whereClause + ") AND i.id >= :startId ORDER BY i.id ASC");
				setParams(q, params);
				q.setParameter("startId", startId);
				q.setMaxResults(batchSize);
				return q.list();
			}
		});
	}

	@Override
	@Transactional(readOnly = true)
	public long getCount(final String joinClause, final String whereClause, final Map<String, Object> params)
	{
		return ((Number) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query q = session
					.createQuery("SELECT COUNT(DISTINCT i.id)  FROM Item i " + joinClause + " WHERE " + whereClause);
				setParams(q, params);
				return q.uniqueResult();
			}
		})).longValue();
	}

	private void setParams(Query q, final Map<String, Object> ps)
	{
		for( Map.Entry<String, Object> entry : ps.entrySet() )
		{
			String key = entry.getKey();
			Object obj = entry.getValue();
			if( obj instanceof Collection<?> )
			{
				q.setParameterList(key, (Collection<?>) obj);
			}
			else
			{
				q.setParameter(key, obj);
			}
		}
	}

	@Override
	public List<ItemIdKey> getItemIdKeys(List<Long> ids)
	{
		String hql = "select new com.tle.beans.item.ItemIdKey(i.id, i.uuid, i.version) from Item i where i.id in :ids and i.institution = :institution";
		return getHibernateTemplate().findByNamedParam(hql, new String[]{"ids", "institution"},
			new Object[]{ids, CurrentInstitution.get()});
	}

	@Override
	public ItemIdKey getItemIdKey(Long id)
	{
		String hql = "select new com.tle.beans.item.ItemIdKey(i.id, i.uuid, i.version) from Item i where i.id = :id and i.institution = :institution";
		List<ItemIdKey> keys = getHibernateTemplate().findByNamedParam(hql, new String[]{"id", "institution"},
			new Object[]{id, CurrentInstitution.get()});
		if( keys == null || keys.isEmpty() )
		{
			return null;
		}

		return keys.get(0);
	}

	@Override
	public List<Triple<String, Integer, String>> enumerateItemNames(String whereClause, String[] names, Object[] values)
	{
		String hql = "select new com.tle.common.Triple(uuid, version, name) from Item item " + whereClause;
		return getHibernateTemplate().findByNamedParam(hql, names, values);
	}

	@Override
	public List<Item> getAllVersionsOfItem(String uuid)
	{
		String query = "from Item where institution = ? and uuid = ? order by version desc";
		return getHibernateTemplate().find(query, new Object[]{CurrentInstitution.get(), uuid});
	}

	@Override
	public Map<ItemId, LanguageBundle> getItemNames(Collection<? extends ItemKey> keys)
	{
		return selectForIds("i.name", keys);
	}

	@Override
	public Map<ItemId, Long> getItemNameIds(Collection<? extends ItemKey> keys)
	{
		return selectForIds("i.name.id", keys);
	}

	private <U> Map<ItemId, U> selectForIds(String select, Collection<? extends ItemKey> keys)
	{
		if( keys.isEmpty() )
		{
			return Collections.emptyMap();
		}

		Object[] keyArray = new Object[(2 * keys.size()) + 1];
		StringBuilder hql = new StringBuilder("SELECT i.uuid, i.version, ");
		hql.append(select);
		hql.append(" FROM Item i WHERE ");

		int i = 0;
		hql.append('(');
		for( ItemKey key : keys )
		{
			if( i > 0 )
			{
				hql.append(" OR ");
			}
			keyArray[i++] = key.getUuid();
			keyArray[i++] = key.getVersion();
			hql.append("(i.uuid = ? and i.version = ?)");
		}
		hql.append(") ");

		keyArray[i] = CurrentInstitution.get();
		hql.append(" and i.institution = ?");

		List<Object[]> results = getHibernateTemplate().find(hql.toString(), keyArray);

		Map<ItemId, U> map = new HashMap<ItemId, U>();
		for( Object[] result : results )
		{
			ItemId itemId = new ItemId((String) result[0], (Integer) result[1]);
			U value = (U) result[2];

			U existing = map.put(itemId, value);
			if( existing != null )
			{
				throw new RuntimeException("Found multiple items with same UUID and version number: " + itemId);
			}
		}
		// We want the original order (EQ-1870)
		Map<ItemId, U> sortedResults = new LinkedHashMap<ItemId, U>();
		for( ItemKey key : keys )
		{
			ItemId id = new ItemId(key.getUuid(), key.getVersion());
			sortedResults.put(id, map.get(id));
		}
		return sortedResults;
	}

	@Override
	public String getNameForId(long id)
	{
		Item item = (Item) getHibernateTemplate().get(getPersistentClass(), id);
		if( item != null )
		{
			LanguageBundle nlb = item.getName();
			return LangUtils.isEmpty(nlb) ? item.getUuid() : CurrentLocale.get(nlb);
		}

		return null;
	}

	@Override
	public List<ItemIdKey> listAll(Institution institution)
	{
		String hql = "select new com.tle.beans.item.ItemIdKey(id, uuid, version)" + " from Item where institution = ?";
		return getHibernateTemplate().find(hql, institution);
	}

	@Override
	public void delete(Item entity)
	{
		itemLockingDao.deleteForItem(entity);
		Map<String, ItemDaoExtension> beans = itemDaoTracker.getBeanMap();
		for( ItemDaoExtension extension : beans.values() )
		{
			extension.delete(entity);
		}
		super.delete(entity);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Integer> getAllVersionNumbers(String uuid)
	{
		return getHibernateTemplate().find(
			"select version from Item where uuid = ? and institution = ? order by version asc",
			new Object[]{uuid, CurrentInstitution.get()});
	}

	@Override
	public int getLatestVersion(String uuid)
	{
		List<Integer> allVersionNumbers = getAllVersionNumbers(uuid);
		if( allVersionNumbers.isEmpty() )
		{
			return 1;
		}
		return allVersionNumbers.get(allVersionNumbers.size() - 1);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int getLatestLiveVersion(String uuid)
	{
		List<Integer> list = getHibernateTemplate().find(
			"select version from Item where uuid = ? and status = ? and institution = ? order by version desc",
			new Object[]{uuid, ItemStatus.LIVE.name(), CurrentInstitution.get()});
		if( list.isEmpty() )
		{
			return 1;
		}
		return list.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ItemIdKey getLatestLiveVersionId(String uuid)
	{
		List<ItemIdKey> list = getHibernateTemplate().find(
			"select new com.tle.beans.item.ItemIdKey(id,uuid,version) from Item where uuid = ? and status = ? and institution = ? order by version desc",
			new Object[]{uuid, ItemStatus.LIVE.name(), CurrentInstitution.get()});
		if( list.isEmpty() )
		{
			return null;
		}
		return list.get(0);
	}

	@Override
	@Transactional
	public int updateIndexTimes(final String whereClause, final String[] names, final Object[] values)
	{
		return (Integer) getHibernateTemplate().execute(new DMLPartitioner("Item", "id")
		{
			@Override
			public String getWhereClause()
			{
				return whereClause + " AND institution = :institution AND dateForIndex < :curdate";
			}

			@Override
			public void setWhereParams(Query query)
			{
				query.setParameter("curdate", new Date());
				query.setParameter("institution", CurrentInstitution.get());

				// TODO: Change the parameters of this method to take a
				// Map<String, Object> and use the existing "setParams()" method
				// for applying the map to a query.
				int i = 0;
				for( String name : names )
				{
					Object obj = values[i++];
					if( obj instanceof Collection )
					{
						query.setParameterList(name, (Collection<?>) obj);
					}
					else
					{
						query.setParameter(name, obj);
					}
				}
			}

			@Override
			public String getDmlStart()
			{
				return "UPDATE Item SET dateForIndex = :curdate";
			}

			@Override
			public void setDmlParams(Query q)
			{
				// Nothing additional to set that not already in the where
				// parameters
			}
		});
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Item getExistingItem(ItemKey itemKey)
	{
		Item item = findByItemId(itemKey);
		if( item == null )
		{
			throw new NotFoundException("Item " + itemKey + " doesn't exist");
		}
		return item;
	}

	@Override
	public Item findByItemId(final ItemKey itemKey)
	{
		return findByItemId(itemKey, false);
	}

	protected Item findByItemId(final ItemKey itemKey, final boolean readOnly)
	{
		return (Item) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				String hql = "from Item where uuid = :uuid and version = :version and institution = :i";
				Query query = session.createQuery(hql);
				query.setParameter("uuid", itemKey.getUuid());
				query.setParameter("version", itemKey.getVersion());
				query.setParameter("i", CurrentInstitution.get());
				Item item = (Item) query.uniqueResult();
				if (item != null)
				{
					session.setReadOnly(item, readOnly);
				}
				return item;
			}
		});
	}

	@Override
	public Set<String> getReferencedUsers()
	{
		List<String> list = getHibernateTemplate().findByNamedParam(
			"select owner from Item where institution = :institution", "institution", CurrentInstitution.get());
		Set<String> users = new HashSet<String>(list);

		List<String> collabs = getHibernateTemplate().findByNamedParam(
			"select elements(collaborators) from Item where institution = :institution", "institution",
			CurrentInstitution.get());
		users.addAll(collabs);

		return users;
	}

	@Override
	public void clearHistory(Item item)
	{
		item.getHistory().clear();
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public <T extends IdCloneable> T mergeTwo(T oldObject, T newObject)
	{
		if( newObject != null && newObject.equals(oldObject) )
		{
			return newObject;
		}
		if( newObject == null )
		{
			deleteAny(oldObject);
			return null;
		}
		if( oldObject == null )
		{
			newObject.setId(0);
		}
		else
		{
			newObject.setId(oldObject.getId());
			return mergeAny(newObject);
		}
		return newObject;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		// TODO a more appropriate plugin needed
		itemDaoTracker = new PluginTracker<ItemDaoExtension>(pluginService, "com.tle.core.item", "itemDaoExtension",
			null);
		itemDaoTracker.setBeanKey("class");
	}

	@Override
	@Transactional
	public long getCollectionIdForUuid(String uuid)
	{
		List<Long> collectionIds = getHibernateTemplate().findByNamedParam(
			"select i.itemDefinition.id from Item i where uuid = :uuid and institution = :institution",
			new String[]{"uuid", "institution"}, new Object[]{uuid, CurrentInstitution.get()});
		if( !collectionIds.isEmpty() )
		{
			return collectionIds.get(0);
		}
		return 0;
	}

	@Override
	public Map<String, Object> getItemInfo(String uuid, int version)
	{
		List<Map<String, Object>> itemInfo = getHibernateTemplate().findByNamedParam(
			"SELECT NEW map(i.name.id as name_id, i.description.id as desc_id, i.itemDefinition.name.id as collection_id, i.dateModified as date_mod) FROM Item i WHERE uuid = :uuid AND institution = :institution AND version = :version",
			new String[]{"uuid", "institution", "version"}, new Object[]{uuid, CurrentInstitution.get(), version});
		if( !itemInfo.isEmpty() )
		{
			return itemInfo.get(0);
		}
		return null;
	}

	@Override
	public Attachment getAttachmentByUuid(ItemKey itemId, String uuid)
	{
		List<Attachment> attachments = getHibernateTemplate().findByNamedParam(
			"select a from Item i left join i.attachments a where i.uuid = :uuid"
				+ " and i.version = :version and a.uuid = :auuid and i.institution = :institution",
			new String[]{"uuid", "version", "auuid", "institution"},
			new Object[]{itemId.getUuid(), itemId.getVersion(), uuid, CurrentInstitution.get()});
		if( attachments.isEmpty() )
		{
			return null;
		}
		return attachments.get(0);
	}

	@Override
	public Set<String> unionItemUuidsWithCollectionUuids(final Collection<String> itemUuids,
		final Set<String> collectionIds)
	{
		if( Check.isEmpty(itemUuids) || Check.isEmpty(collectionIds) )
		{
			return Collections.emptySet();
		}

		return new HashSet<String>(getHibernateTemplate().executeFind(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				final Query query = session.createQuery("SELECT DISTINCT(i.uuid) FROM Item i"
					+ " WHERE i.uuid IN (:uuids) AND i.itemDefinition.uuid IN (:itemDefIds)"
					+ " AND i.institution = :institution");
				query.setParameterList("uuids", itemUuids);
				query.setParameterList("itemDefIds", collectionIds);
				query.setParameter("institution", CurrentInstitution.get());
				return query.list();
			}
		}));
	}

	@Override
	public List<Item> getItems(List<String> uuids, Institution institution)
	{
		if( uuids.isEmpty() )
		{
			return Collections.emptyList();
		}

		List<Item> itemList = findAllByCriteria(Restrictions.in("uuid", uuids),
			Restrictions.eq("institution", institution));

		Map<String, Item> itemMap = new HashMap<String, Item>(itemList.size());
		for( Item item : itemList )
		{
			itemMap.put(item.getUuid(), item);
		}

		List<Item> results = new ArrayList<Item>(uuids.size());

		for( String uuid : uuids )
		{
			// null's need to be inserted if the item doesn't exist
			results.add(itemMap.get(uuid));
		}
		return results;
	}

	@Override
	public Map<ItemId, Item> getItems(List<? extends ItemKey> itemIds)
	{
		return selectForIds("i", itemIds);
	}

	@Override
	public List<Integer> getCommentCounts(List<Item> items)
	{
		if( items.isEmpty() )
		{
			return Collections.emptyList();
		}
		Map<Long, Integer> commentMap = new LinkedHashMap<Long, Integer>();
		List<Object[]> counts = getHibernateTemplate().findByNamedParam(
			"select i.id, count(c) from Item i join i.comments c where i in (:items) group by i", "items", items);
		for( Object[] comRow : counts )
		{
			commentMap.put((Long) comRow[0], ((Number) comRow[1]).intValue());
		}
		List<Integer> countList = new ArrayList<Integer>();
		for( Item item : items )
		{
			Integer count = commentMap.get(item.getId());
			if( count == null )
			{
				count = 0;
			}
			countList.add(count);
		}
		return countList;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ListMultimap<Item, Attachment> getAttachmentsForItems(Collection<Item> items)
	{
		if( items.isEmpty() )
		{
			return ImmutableListMultimap.of();
		}
		List<Attachment> attachments = getHibernateTemplate().findByNamedParam(
			"select a from Item i join i.attachments a where i in (:items) order by index(a) ASC", "items", items);
		ListMultimap<Item, Attachment> multiMap = ArrayListMultimap.create();
		for( Attachment attachment : attachments )
		{
			multiMap.put(attachment.getItem(), attachment);
		}
		return multiMap;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ListMultimap<Long, Attachment> getAttachmentsForItemIds(Collection<Long> ids)
	{
		if( ids.isEmpty() )
		{
			return ImmutableListMultimap.of();
		}
		List<Object[]> attachments = getHibernateTemplate().findByNamedParam(
			"select a, i.id from Item i join i.attachments a where i.id in (:items) order by index(a) ASC", "items",
			ids);
		ListMultimap<Long, Attachment> multiMap = ArrayListMultimap.create();
		for( Object[] attachmentRow : attachments )
		{
			multiMap.put((Long) attachmentRow[1], (Attachment) attachmentRow[0]);
		}
		return multiMap;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ListMultimap<Long, HistoryEvent> getHistoryForItemIds(Collection<Long> ids)
	{
		if( ids.isEmpty() )
		{
			return ImmutableListMultimap.of();
		}
		List<Object[]> history = getHibernateTemplate().findByNamedParam(
			"select h, i.id from Item i join i.history h where i.id in (:items) order by index(h)", "items", ids);
		ListMultimap<Long, HistoryEvent> multiMap = ArrayListMultimap.create();
		for( Object[] historyRow : history )
		{
			multiMap.put((Long) historyRow[1], (HistoryEvent) historyRow[0]);
		}
		return multiMap;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ListMultimap<Long, ItemNavigationNode> getNavigationNodesForItemIds(Collection<Long> ids)
	{
		if( ids.isEmpty() )
		{
			return ImmutableListMultimap.of();
		}
		List<Object[]> node = getHibernateTemplate().findByNamedParam(
			"select n, i.id from ItemNavigationNode n join n.item i where i.id in (:items) order by n.index ASC",
			"items", ids);
		ListMultimap<Long, ItemNavigationNode> multiMap = ArrayListMultimap.create();
		for( Object[] nodeRow : node )
		{
			multiMap.put((Long) nodeRow[1], (ItemNavigationNode) nodeRow[0]);
		}
		return multiMap;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Multimap<Long, String> getCollaboratorsForItemIds(Collection<Long> itemIds)
	{
		if( itemIds.isEmpty() )
		{
			return ImmutableMultimap.of();
		}
		List<Object[]> attachments = getHibernateTemplate().findByNamedParam(
			"select c, i.id from Item i join i.collaborators c where i.id in (:items)", "items", itemIds);
		ListMultimap<Long, String> multiMap = ArrayListMultimap.create();
		for( Object[] attachmentRow : attachments )
		{
			multiMap.put((Long) attachmentRow[1], (String) attachmentRow[0]);
		}
		return multiMap;
	}

	@Override
	public ListMultimap<Long, DrmAcceptance> getDrmAcceptancesForItemIds(Collection<Long> itemIds)
	{
		if( itemIds.isEmpty() )
		{
			return ImmutableListMultimap.of();
		}
		final List<Object[]> drmAcceptances = getHibernateTemplate().findByNamedParam(
			"select d, item.id from DrmAcceptance d where item.id in (:items) order by d.date DESC", "items", itemIds);
		final ListMultimap<Long, DrmAcceptance> multiMap = ArrayListMultimap.create();
		for( Object[] asseptRow : drmAcceptances )
		{
			multiMap.put((Long) asseptRow[1], (DrmAcceptance) asseptRow[0]);
		}
		return multiMap;
	}

	@Override
	public Attachment getAttachmentByFilepath(ItemKey itemId, String filepath)
	{
		List<Attachment> attachments = getHibernateTemplate().findByNamedParam(
			"select a from Item i left join i.attachments a where i.uuid = :uuid"
				+ " and i.version = :version and a.url = :filepath and "
				+ "a.class = FileAttachment and i.institution = :institution",
			new String[]{"uuid", "version", "filepath", "institution"},
			new Object[]{itemId.getUuid(), itemId.getVersion(), filepath, CurrentInstitution.get()});
		if( attachments.isEmpty() )
		{
			return null;
		}
		return attachments.get(0);
	}

	/**
	 * Translation into raw SQL when there is an empty set for the clause<br>
	 * ... WHERE i IN (:items) AND ...<br>
	 * will yield invalid SQL.<br>
	 * ... WHERE i.id IN () AND t.attachment_id = a.id;<br>
	 * So if the items parameter is ever empty, return an empty result set
	 * without troubling Hibernate.
	 * 
	 * @see com.tle.core.dao.ItemDao#getNavReferencedAttachmentIds(java.util.List)
	 */
	@Override
	public List<String> getNavReferencedAttachmentUuids(List<Item> items)
	{
		if( !Check.isEmpty(items) )
		{
			return getHibernateTemplate().findByNamedParam(
				"SELECT a.uuid FROM Attachment a JOIN a.item i JOIN i.treeNodes tn JOIN tn.tabs t WHERE i IN (:items) AND t.attachment = a",
				"items", items);
		}
		return new ArrayList<String>();
	}
}
