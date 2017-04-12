package com.tle.core.zookeeper;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;

public interface ZookeeperService
{
	boolean hasStarted();

	boolean isCluster();

	boolean isClusterDebugging();

	void setClusterDebugging(boolean debug);

	String getNodeId();

	CuratorFramework getCurator();

	List<String> getAppServers();

	PathChildrenCache createPathCache(String type, boolean cacheData, PathChildrenCacheListener listener);

	PathChildrenCache createPathCache(String type, boolean cacheData, PathChildrenCacheListener listener,
		StartMode startMode);

	PathChildrenCache createPathCache(String type, boolean cacheData);

	PersistentEphemeralNode createNode(String type, String data);

	String getFullPath(String type, String... extras);
}
