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

package com.tle.core.activecache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.activecache.settings.CacheSettings;
import com.tle.common.activecache.settings.CacheSettings.Node;
import com.tle.common.activecache.settings.CacheSettings.Query;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.search.whereparser.WhereParser;
import com.tle.common.searching.SearchResults;
import com.tle.common.util.Dates;
import com.tle.common.util.LocalDate;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;

/**
 * Used to calculate the list of urls required by Active Caches. These urls are
 * returns as a string array with a '+' or '-' depending on the updated status
 * of the url and whether it should be added or removed from the Cache. It is
 * possible that the list of queries for a particular user may become
 * "Out of Sync" with the original queries used. In this case an attempt is made
 * to "Re-Sync" these lists by doing the original queries "In reverse" so to
 * speak, and to undo any urls which may have been missed had the new queries
 * been run originally. (ie If an exclude query is removed, hopefully we can
 * "undo" this and have any urls for these excluded items added this time
 * round). Unfortunately this requires a larger set of queries and should
 * perhaps only be performed at times when the database is not being used.
 * 
 * @author Charles O'Farrell
 * @dytech.jira see Jira SCR TLE-1064 :
 *              http://apps.dytech.com.au/jira/browse/TLE-1064
 */
public class CacheHelper
{
	private static final String LIVECHECK = status(ItemStatus.LIVE);

	private final CacheInterface bean;
	private final String userid;
	private final Collection<CacheQuery> queries;
	private final CacheSettings cs;

	private Set<ItemKey> added;
	private Set<ItemKey> removed;

	public CacheHelper(CacheInterface bean, CacheSettings cs, String userid)
	{
		this.cs = cs;
		this.userid = userid;
		this.bean = bean;
		queries = getCurrentQueries();
	}

	public List<String> getCacheList(Calendar current, String lastUpdate) throws Exception
	{
		// Don't need these till now
		added = new HashSet<ItemKey>();
		removed = new HashSet<ItemKey>();

		Date currentDate = current.getTime();
		List<String> items = new ArrayList<String>();
		items.add(new LocalDate(currentDate, CurrentTimeZone.get()).format(Dates.ISO_WITH_TIMEZONE));

		List<CacheQuery> previous = getPreviousQueries();
		if( !inSync(previous) )
		{
			getCacheListSync(lastUpdate, previous);
		}
		else
		{
			getCacheListUpdate(lastUpdate);
		}
		saveQueries(current);
		addItemsToList(items, added, "+");
		addItemsToList(items, removed, "-");

		return items;
	}

	private boolean inSync(Collection<CacheQuery> previous)
	{
		// NOT in Sync, but probably first time
		boolean inSync = previous.size() == 0;
		if( !inSync )
		{
			inSync = previous.size() == queries.size();

			if( inSync )
			{
				Iterator<CacheQuery> iter = previous.iterator();
				Iterator<CacheQuery> iter2 = queries.iterator();
				while( inSync && iter.hasNext() )
				{
					CacheQuery oldQuery = iter.next();
					CacheQuery newQuery = iter2.next();

					boolean b1 = oldQuery.getQuery().equals(newQuery.getQuery());
					boolean b2 = oldQuery.isInclude() == newQuery.isInclude();

					inSync = b1 && b2;
				}
			}
		}
		return inSync;
	}

	private void getCacheListSync(String lastUpdate, Collection<CacheQuery> previousQueries) throws Exception
	{
		String extraQuery = dateModified("<=", lastUpdate);
		query(previousQueries, extraQuery, true, false);

		extraQuery = '(' + LIVECHECK + ')';
		query(queries, extraQuery, false, false);
	}

	private static String dateModified(String con, String date)
	{
		return "/xml/item/datemodified " + con + " '" + date + "'";
	}

	private static String status(ItemStatus status)
	{
		return "/xml/item/@itemstatus = '" + status + "'";
	}

	private void getCacheListUpdate(String lastUpdate) throws Exception
	{
		// queryNewItemds SHOULD Be the following:
		// liveapprovaldate > lastUpdate or (datecreated > lastUpdate && live &&
		// moderating)
		String queryBase = dateModified(">", lastUpdate) + " AND "; //$NON-NLS-2$

		final String queryNewItems = queryBase + LIVECHECK;

		final String queryOldItems = queryBase + '(' + status(ItemStatus.SUSPENDED) + " OR "
			+ status(ItemStatus.DELETED) + ')';

		query(queries, queryNewItems, false, true);
		query(queries, queryOldItems, true, true);
	}

	private void query(Collection<CacheQuery> querySet, String extraQuery, boolean invert, boolean update)
	{
		for( CacheQuery query : querySet )
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append(" WHERE (");
			String qq = query.getQuery();
			if( !Check.isEmpty(qq) )
			{
				buffer.append(qq);
				buffer.append(") AND (");
			}
			buffer.append(extraQuery);
			buffer.append(')');

			String queryBase = buffer.toString();

			FreeTextBooleanQuery query2 = WhereParser.parse(queryBase);

			long itemdefid = query.getItemdefid();
			String uuid = query.getUuid();

			SearchResults<ItemIdKey> results;
			if( !Check.isEmpty(uuid) )
			{
				results = bean.query(query2, uuid);
			}
			else
			{
				results = bean.query(query2, itemdefid);
			}
			if( results != null )
			{
				for( ItemKey key : results.getResults() )
				{
					if( !invert )
					{
						addItem(query.isInclude(), key, update);
					}
					else
					{
						addItem(!query.isInclude(), key, update);
					}
				}
			}
		}
	}

	private void addItemsToList(Collection<String> list, Set<ItemKey> set, String symbol)
	{
		for( ItemKey key : set )
		{
			list.add(symbol + key.toString());
		}
	}

	private void addItem(boolean include, ItemKey key, boolean update)
	{
		if( include )
		{
			boolean contains = removed.contains(key);
			if( contains )
			{
				removed.remove(key);
			}

			if( !contains || update )
			{
				added.add(key);
			}
		}
		else
		{
			boolean contains = added.contains(key);
			if( contains )
			{
				added.remove(key);
			}

			if( !contains || update )
			{
				removed.add(key);
			}
		}
	}

	List<CacheQuery> getCurrentQueries()
	{
		List<CacheQuery> list = getCurrentQueries(cs.getGroups());
		if( list == null )
		{
			list = new ArrayList<CacheQuery>();
			getQueries(list, cs.getGroups());
		}
		return list;
	}

	private List<CacheQuery> getCurrentQueries(Node group)
	{
		List<CacheQuery> found = null;
		if( group != null )
		{
			for( Node node : group.getNodes() )
			{
				boolean localFound = false;
				List<CacheQuery> localList = null;
				if( node.isUser() )
				{
					localFound = node.getId().equals(userid);
				}
				else
				{
					localList = getCurrentQueries(node);
				}

				if( localFound || localList != null )
				{
					if( found == null )
					{
						found = new ArrayList<CacheQuery>();
						getQueries(found, group);
					}

					if( localList != null )
					{
						found.addAll(localList);
					}

					getQueries(found, node);
				}
			}
		}

		return found;
	}

	private void getQueries(Collection<CacheQuery> list, Node group)
	{
		if( group != null )
		{
			for( Query q : group.getIncludes() )
			{
				CacheQuery query = new CacheQuery();
				query.setQuery(q.getScript());
				query.setInclude(true);

				String uuid = q.getUuid();

				if( !Check.isEmpty(uuid) )
				{
					query.setUuid(uuid);
				}
				else
				{
					query.setUuid(bean.convertItemDefIdToUuid(q.getItemdef()));
				}
				list.add(query);
			}
			for( Query q : group.getExcludes() )
			{
				CacheQuery query = new CacheQuery();
				query.setQuery(q.getScript());
				query.setInclude(false);
				String uuid = q.getUuid();

				if( !Check.isEmpty(uuid) )
				{
					query.setUuid(uuid);
				}
				else
				{
					query.setUuid(bean.convertItemDefIdToUuid(q.getItemdef()));
				}
				list.add(query);
			}
		}
	}

	private List<CacheQuery> getPreviousQueries()
	{
		List<CacheQuery> list = new ArrayList<CacheQuery>();

		PropBagEx settings = bean.getActiveCacheSettings(userid);
		for( PropBagEx xml : settings.iterator() )
		{
			CacheQuery query = new CacheQuery();
			query.setQuery(xml.getNode(Constants.BLANK));
			query.setInclude(xml.getNodeName().equals("include"));

			try
			{
				long id = Long.parseLong(xml.getNode("@itemdefid"));
				if( id > 0 )
				{
					query.setUuid(bean.convertItemDefIdToUuid(id));
				}
				else
				{
					query.setUuid(xml.getNode("@uuid"));
				}
			}
			catch( NumberFormatException e )
			{
				query.setUuid(xml.getNode("@uuid"));
			}

			list.add(query);
		}

		return list;
	}

	private void saveQueries(Calendar current)
	{
		PropBagEx settings = bean.getActiveCacheSettings(userid);

		// Remove old settings
		settings.deleteAll("include");
		settings.deleteAll("exclude");

		// Not really used yet, but it can't hurt to store now.
		settings.setNode("@lastUpdate", new LocalDate(current, CurrentTimeZone.get()).format(Dates.ISO_WITH_TIMEZONE));

		for( CacheQuery query : queries )
		{
			PropBagEx ex = settings.newSubtree(query.isInclude() ? "include" : "exclude"); //$NON-NLS-2$
			String uuid = query.getUuid();
			if( !Check.isEmpty(uuid) )
			{
				ex.setNode("@uuid", uuid);
			}
			else
			{
				ex.setNode("@itemdefid", query.getItemdefid());
			}
			ex.setNode(Constants.BLANK, Check.nullToEmpty(query.getQuery()));
		}
		bean.updateActiveCacheSettings(userid, settings);
	}

	public boolean userExists()
	{
		return queries != null && queries.size() > 0;
	}

	/**
	 * An interface for caching objects to implement.
	 */
	public interface CacheInterface
	{
		void updateActiveCacheSettings(String userid, PropBagEx settings);

		PropBagEx getActiveCacheSettings(String userid);

		SearchResults<ItemIdKey> query(FreeTextBooleanQuery query, long itemdefid);

		SearchResults<ItemIdKey> query(FreeTextBooleanQuery query, String uuid);

		String convertItemDefIdToUuid(long itemDefId);
	}

	/**
	 * Holds a query.
	 */
	private static class CacheQuery
	{
		private String query;
		private boolean include;
		private long itemdefid;
		private String uuid;

		public CacheQuery()
		{
			super();
		}

		@Override
		public String toString()
		{
			return (include ? "i" : "e") + ":" + query;
		}

		/**
		 * @return Returns the include.
		 */
		public boolean isInclude()
		{
			return include;
		}

		/**
		 * @param include The include to set.
		 */
		public void setInclude(boolean include)
		{
			this.include = include;
		}

		/**
		 * @return Returns the query.
		 */
		public String getQuery()
		{
			return query;
		}

		/**
		 * @param query The query to set.
		 */
		public void setQuery(String query)
		{
			this.query = query;
		}

		public long getItemdefid()
		{
			return itemdefid;
		}

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}

		public String getUuid()
		{
			return uuid;
		}
	}
}
