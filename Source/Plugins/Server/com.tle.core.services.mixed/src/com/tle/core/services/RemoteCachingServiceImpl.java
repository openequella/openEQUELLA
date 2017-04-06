package com.tle.core.services;

import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.ejb.helpers.CacheHelper;
import com.dytech.edge.ejb.helpers.CacheHelper.CacheInterface;
import com.dytech.edge.exceptions.NotFoundException;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.system.CacheSettings;
import com.tle.beans.system.CacheSettings.Node;
import com.tle.beans.system.CacheSettings.Query;
import com.tle.common.Check;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.SearchResults;
import com.tle.core.events.listeners.ItemDefinitionDeletionListener;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.guice.Bind;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.core.services.item.FreeTextService;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.user.CurrentUser;

@Bind(RemoteCachingService.class)
@Singleton
public class RemoteCachingServiceImpl implements RemoteCachingService, ItemDefinitionDeletionListener
{
	private static final Logger LOGGER = Logger.getLogger(RemoteCachingServiceImpl.class);

	private Object cacheLock = new Object();

	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private FreeTextService freeTextService;
	@Inject
	private UserPreferenceService userPreferenceService;

	public RemoteCachingServiceImpl()
	{
		super();
	}

	/**
	 * Returns list of item defs required to be updated.
	 */
	@Override
	public List<String> getCacheList(String lastUpdate) throws Exception
	{
		synchronized( cacheLock )
		{
			Calendar current = Calendar.getInstance();
			CacheSettings cs = getCachingSettings();

			String userid = CurrentUser.getUserID();

			CacheHelper helper = new CacheHelper(new CacheBean(), cs, userid);
			if( !cs.isEnabled() )
			{
				throw new Exception("Remote caching is currently disabled");
			}

			if( !helper.userExists() )
			{
				LOGGER.warn(userid + " attempted to schedule cache time, but does not have the authority");
				throw new Exception("You do not have permission to remotely cache");
			}

			LOGGER.info("Accepting " + userid + " for caching.  Last update was on " + lastUpdate);

			setCachingSettings(cs);

			// see Jira SCR TLE-1064 :
			// http://apps.dytech.com.au/jira/browse/TLE-1064
			return helper.getCacheList(current, lastUpdate);
		}
	}

	private CacheSettings getCachingSettings()
	{
		return configService.getProperties(new CacheSettings());
	}

	private void setCachingSettings(CacheSettings cs)
	{
		configService.setProperties(cs);
	}

	/**
	 * Implementation for accessing the repository.
	 */
	private class CacheBean implements CacheInterface
	{
		public CacheBean()
		{
			//
		}

		@Override
		public void updateActiveCacheSettings(String userid, PropBagEx settings)
		{
			userPreferenceService.setRemoteCachingPreferences(settings);
		}

		@Override
		public PropBagEx getActiveCacheSettings(String userid)
		{
			PropBagEx remoteCachingPreferences = userPreferenceService.getRemoteCachingPreferences();
			for( PropBagEx xml : remoteCachingPreferences.iterator() )
			{
				try
				{
					String node = xml.getNode("@itemdefid");
					if( !Check.isEmpty(node) && Long.parseLong(node) > 0 )
					{
						ItemDefinition itemDefinition = itemDefinitionService.get(Long.parseLong(node));
						xml.setNode("@uuid", itemDefinition.getUuid());
						xml.deleteNode("@itemdefid");
					}
				}
				catch( NotFoundException e )
				{
					LOGGER.warn(e.getLocalizedMessage());
				}
			}
			return remoteCachingPreferences;
		}

		@Override
		public SearchResults<ItemIdKey> query(FreeTextBooleanQuery query, long itemdefid)
		{
			try
			{
				DefaultSearch search = new DefaultSearch();
				search.setFreeTextQuery(query);
				if( itemdefid > 0 )
				{
					search.setCollectionUuids(Collections.singletonList(itemDefinitionService.getUuidForId(itemdefid)));
				}
				return freeTextService.searchIds(search, 0, Integer.MAX_VALUE);
			}
			catch( NotFoundException e )
			{
				LOGGER.warn(e.getLocalizedMessage());
				return null;
			}
		}

		@Override
		public SearchResults<ItemIdKey> query(FreeTextBooleanQuery query, String uuid)
		{
			try
			{
				DefaultSearch search = new DefaultSearch();
				search.setFreeTextQuery(query);
				if( !Check.isEmpty(uuid) )
				{
					search.setCollectionUuids(Collections.singletonList(uuid));
				}
				return freeTextService.searchIds(search, 0, Integer.MAX_VALUE);
			}
			catch( NotFoundException e )
			{
				LOGGER.warn(e.getLocalizedMessage());
				return null;
			}
		}

		@Override
		public String convertItemDefIdToUuid(long itemDefId)
		{
			ItemDefinition itemDef = itemDefinitionService.get(itemDefId);
			return itemDef.getUuid();
		}
	}

	@Override
	public void removeReferences(ItemDefinition idef)
	{
		synchronized( cacheLock )
		{
			CacheSettings cs = getCachingSettings();
			Node groups = cs.getGroups();
			if( groups != null )
			{
				clearNodes(groups, idef.getUuid(), idef.getId());
			}
			cs.setGroups(groups);
			setCachingSettings(cs);
		}
	}

	private void clearNodes(Node node, String uuid, long id)
	{
		List<Node> nodes = node.getNodes();
		if( !Check.isEmpty(nodes) )
		{
			for( Node n : nodes )
			{
				clearNodes(n, uuid, id);
			}
		}

		clearQueries(node.getIncludes().iterator(), uuid, id);
		clearQueries(node.getExcludes().iterator(), uuid, id);
	}

	private void clearQueries(Iterator<Query> iter, String uuid, long id)
	{
		while( iter.hasNext() )
		{
			Query query = iter.next();
			if( Objects.equals(query.getUuid(), uuid) || Objects.equals(query.getItemdef(), id) )
			{
				iter.remove();
			}
		}
	}
}
