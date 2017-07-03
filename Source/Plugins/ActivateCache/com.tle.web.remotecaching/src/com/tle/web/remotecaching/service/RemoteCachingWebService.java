package com.tle.web.remotecaching.service;

import java.util.Map;

import com.tle.common.activecache.settings.CacheSettings;
import com.tle.common.activecache.settings.CacheSettings.Node;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public interface RemoteCachingWebService
{
	String KEY_ROOT = "";

	void abandonCurrentChanges();

	void save(boolean enabled, Node rootNode);

	CacheSettings getCacheSettings();

	Map<String, Node> getNodeCache();
}
