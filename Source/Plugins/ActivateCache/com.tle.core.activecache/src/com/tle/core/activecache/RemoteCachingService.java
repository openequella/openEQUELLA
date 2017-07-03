/*
 * Created on 12/12/2005
 */
package com.tle.core.activecache;

import java.util.List;

public interface RemoteCachingService
{
	List<String> getCacheList(String lastUpdate) throws Exception;
}
