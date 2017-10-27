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

package com.tle.web.remotecaching.service;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.tle.beans.Institution;
import com.tle.common.activecache.settings.CacheSettings;
import com.tle.common.activecache.settings.CacheSettings.Node;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.settings.events.ConfigurationChangedEvent;
import com.tle.core.settings.events.listeners.ConfigurationChangeListener;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.sections.equella.annotation.PlugKey;

@Bind(RemoteCachingWebService.class)
@Singleton
public class RemoteCachingWebServiceImpl implements RemoteCachingWebService, ConfigurationChangeListener
{
	private final Cache<CacheKey, Map<String, Node>> nodeCache = CacheBuilder.newBuilder()
		.expireAfterAccess(1, TimeUnit.DAYS).build();

	@Inject
	private ConfigurationService configService;

	@PlugKey("remotecaching.default.rootnode.name")
	private static String DEFAULT_NODE_EVERYONE_KEY;

	@Override
	public void abandonCurrentChanges()
	{
		invalidateCache();
	}

	@Override
	public void save(boolean enabled, Node rootNode)
	{
		final CacheSettings settings = getCacheSettings();
		settings.setEnabled(enabled);
		settings.setGroups(rootNode);
		configService.setProperties(settings);
		invalidateCache();
	}

	@Override
	public CacheSettings getCacheSettings()
	{
		return configService.getProperties(new CacheSettings());
	}

	@Override
	public Map<String, Node> getNodeCache()
	{
		final Institution inst = CurrentInstitution.get();
		final String sessionId = CurrentUser.getSessionID();

		return getNodeMap(inst.getUniqueId(), sessionId);
	}

	@Override
	public void configurationChangedEvent(ConfigurationChangedEvent event)
	{
		invalidateCache();
	}

	private void invalidateCache()
	{
		final Institution inst = CurrentInstitution.get();
		synchronized( nodeCache )
		{
			for( CacheKey key : new HashSet<CacheKey>(nodeCache.asMap().keySet()) )
			{
				if( key.getInstitutionId() == inst.getUniqueId() )
				{
					nodeCache.invalidate(key);
				}
			}
		}
	}

	private Map<String, Node> getNodeMap(long institutionId, String sessionId)
	{
		final CacheKey key = new CacheKey(institutionId, sessionId);
		synchronized( nodeCache )
		{
			Map<String, Node> map = nodeCache.getIfPresent(key);
			if( map == null )
			{
				map = Maps.newHashMap();

				Node root = getCacheSettings().getGroups();
				if( root == null || empty(root) )
				{
					String defaultName = CurrentLocale.get(DEFAULT_NODE_EVERYONE_KEY);
					root = new Node(defaultName, true);
				}

				map.put(KEY_ROOT, root);
				buildMap(map, root);

				nodeCache.put(key, map);
			}
			return map;
		}
	}

	/**
	 * I'm not convinced this is a real-world case... anyway, it *can* happen if
	 * your config property for cache.groups is &lt;groups&gt;
	 * 
	 * @param node
	 * @return
	 */
	private boolean empty(Node node)
	{
		return (node.getId() == null && node.getName() == null && node.getUuid() == null);
	}

	private void buildMap(Map<String, Node> map, Node node)
	{
		map.put(getId(node), node);
		for( Node child : node.getNodes() )
		{
			buildMap(map, child);
		}
	}

	/**
	 * What a load of bollocks. Why didn't they just have a unique ID in the
	 * first place!
	 */
	private String getId(Node node)
	{
		if( node.getUuid() == null )
		{
			node.setUuid(UUID.randomUUID().toString());
			return node.getUuid();
		}
		return node.getUuid();
	}

	private static class CacheKey
	{
		private final long institutionId;
		private final String sessionId;

		public CacheKey(long institutionId, String sessionId)
		{
			this.institutionId = institutionId;
			this.sessionId = sessionId;
		}

		public long getInstitutionId()
		{
			return institutionId;
		}

		@Override
		public boolean equals(Object other)
		{
			if( other instanceof CacheKey )
			{
				final CacheKey otherKey = (CacheKey) other;
				if( otherKey.institutionId == institutionId && otherKey.sessionId.equals(sessionId) )
				{
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return sessionId.hashCode() + (int) institutionId;
		}
	}
}
