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
