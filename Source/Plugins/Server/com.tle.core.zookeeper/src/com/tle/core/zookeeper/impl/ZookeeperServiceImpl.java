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

package com.tle.core.zookeeper.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode.Mode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.EnsurePath;
import org.apache.curator.utils.ZKPaths;
import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tle.common.Check;
import com.tle.core.application.StartupBean;
import com.tle.core.guice.Bind;
import com.tle.core.zookeeper.ZookeeperService;

@Bind(ZookeeperService.class)
@Singleton
@SuppressWarnings("nls")
public class ZookeeperServiceImpl implements ZookeeperService, StartupBean
{
	private static final Logger LOGGER = Logger.getLogger(ZookeeperServiceImpl.class);

	private static final String BASE_FULLPATH = "equella";
	private static final String SERVER_REL_PATH = "server";
	private static final String CONFIG_FULL_PATH = ZKPaths.makePath(BASE_FULLPATH, "config");
	private static final String CLUSTER_DEBUG_FULL_PATH = ZKPaths.makePath(CONFIG_FULL_PATH, "clusterdebug");

	@Inject(optional = true)
	@Named("zookeeper.instances")
	private String zooKeeperInstances;
	@Inject
	@Named("zookeeper.prefix")
	private String zooKeeperPrefix;
	@Inject
	@Named("zookeeper.nodeId")
	private String zooKeeperNodeId;

	private CuratorFramework client;
	private NodeCache debugCache;

	private PathChildrenCache membersCache;

	private EnsurePath ensureDebug;

	private boolean hasStarted;

	@Override
	public boolean hasStarted()
	{
		return hasStarted;
	}

	@Override
	public boolean isCluster()
	{
		return !Check.isEmpty(zooKeeperInstances);
	}

	@Override
	public boolean isClusterDebugging()
	{
		if( isCluster() )
		{
			ChildData currentData = debugCache.getCurrentData();
			return currentData != null && Boolean.parseBoolean(new String(currentData.getData()));
		}
		return false;
	}

	@Override
	public void setClusterDebugging(boolean debug)
	{
		try
		{
			CuratorFramework curator = getCurator();
			ensureDebug.ensure(curator.getZookeeperClient());
			curator.setData().forPath(CLUSTER_DEBUG_FULL_PATH, Boolean.toString(debug).getBytes());
		}
		catch( Exception e )
		{
			LOGGER.warn("could not set cluster debug status");
		}
	}

	@PostConstruct
	private void setupNodeId()
	{
		String randomUuid = UUID.randomUUID().toString();
		if( Check.isEmpty(zooKeeperNodeId) )
		{
			try
			{
				String hostname = InetAddress.getLocalHost().getHostName();
				zooKeeperNodeId = !Check.isEmpty(hostname) ? MessageFormat.format("{0}-{1}", hostname, randomUuid)
					: randomUuid;
			}
			catch( UnknownHostException e )
			{
				LOGGER.warn("Unable to retrieve hostname to generate node ID. Using random ID");
				zooKeeperNodeId = randomUuid;
			}
		}
		else
		{
			zooKeeperNodeId = MessageFormat.format("{0}-{1}", zooKeeperNodeId, randomUuid);
		}
	}

	@Override
	public void startup()
	{
		if( isCluster() )
		{
			CuratorFramework curator = getCurator();
			debugCache = new NodeCache(curator, CLUSTER_DEBUG_FULL_PATH);
			try
			{
				debugCache.start();
				createNode(SERVER_REL_PATH, "");
				membersCache = createPathCache(SERVER_REL_PATH, false);
				ensureDebug = curator.newNamespaceAwareEnsurePath(CLUSTER_DEBUG_FULL_PATH);
			}
			catch( Exception e )
			{
				Throwables.propagate(e);
			}
		}
		hasStarted = true;
	}

	@Override
	public PathChildrenCache createPathCache(String type, boolean cacheData)
	{
		return createPathCache(type, cacheData, null, StartMode.NORMAL);
	}

	@Override
	public PathChildrenCache createPathCache(String type, boolean cacheData, PathChildrenCacheListener listener)
	{
		return createPathCache(type, cacheData, listener, StartMode.NORMAL);
	}

	@Override
	public PathChildrenCache createPathCache(String type, boolean cacheData, PathChildrenCacheListener listener,
		StartMode startMode)
	{
		try
		{
			PathChildrenCache cache = new PathChildrenCache(client, getParentPath(type), cacheData);
			if( listener != null )
			{
				cache.getListenable().addListener(listener);
			}
			cache.start(startMode);
			return cache;
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public String getNodeId()
	{
		return zooKeeperNodeId;
	}

	@Override
	public synchronized CuratorFramework getCurator()
	{
		if( !isCluster() )
		{
			return null;
		}
		if( client == null )
		{
			// Initialise Curator client
			String chroot = Check.isEmpty(zooKeeperPrefix) ? "" : zooKeeperPrefix.charAt(0) == '/' ? zooKeeperPrefix
				: "/" + zooKeeperPrefix;
			client = CuratorFrameworkFactory.newClient(zooKeeperInstances.trim() + chroot, 10000, 10000,
				new ExponentialBackoffRetry(1000, 3));
			client.start();

			try
			{
				client.newNamespaceAwareEnsurePath("/").ensure(client.getZookeeperClient());
			}
			catch( Exception e1 )
			{
				// nothing
			}
		}
		return client;
	}

	@Override
	public List<String> getAppServers()
	{
		return Lists.transform(membersCache.getCurrentData(), new Function<ChildData, String>()
		{
			@Override
			public String apply(ChildData input)
			{
				return ZKPaths.getNodeFromPath(input.getPath());
			}
		});
	}

	@Override
	public PersistentEphemeralNode createNode(String type, String data)
	{
		PersistentEphemeralNode node = new PersistentEphemeralNode(getCurator(), Mode.EPHEMERAL, getNodePath(type),
			data.getBytes());
		node.start();
		return node;
	}

	private String getNodePath(String type)
	{
		return ZKPaths.makePath(getParentPath(type), getNodeId());
	}

	private String getParentPath(String type)
	{
		return ZKPaths.makePath(BASE_FULLPATH, type);
	}

	@Override
	public String getFullPath(String type, String... extras)
	{
		String path = getParentPath(type);
		for( String child : extras )
		{
			path = ZKPaths.makePath(path, child);
		}
		return path;
	}
}
