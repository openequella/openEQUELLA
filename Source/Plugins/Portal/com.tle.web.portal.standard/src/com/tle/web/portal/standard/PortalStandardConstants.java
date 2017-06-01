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

package com.tle.web.portal.standard;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;

/**
 * Yes, this is a component that needs to be autowired. The idea is that all
 * settings can be over-ridden in optional.properties HOWEVER, they should
 * really be system settings.
 * 
 * @author aholland
 */
@Bind
@Singleton
public class PortalStandardConstants
{
	public static final String FEED_CACHE_FILE = "rss.xml"; //$NON-NLS-1$
	private static final int FEED_MAX_RESULTS = 30;
	/**
	 * 10MB should be an AMPLE default
	 */
	private static final int FEED_MAX_BYTE_SIZE = 10 * 1024 * 1024;
	/**
	 * two hours is a reasonable max session time I'd say
	 */
	private static final long FEED_CACHE_TIMEOUT = TimeUnit.DAYS.toMillis(2);

	private int maxRssResults = FEED_MAX_RESULTS;
	private int maxRssByteSize = FEED_MAX_BYTE_SIZE;
	private long rssCacheTimeout = FEED_CACHE_TIMEOUT;

	public int getMaxRssResults()
	{
		return maxRssResults;
	}

	public void setMaxRssResults(int maxRssResults)
	{
		this.maxRssResults = maxRssResults;
	}

	public int getMaxRssByteSize()
	{
		return maxRssByteSize;
	}

	public void setMaxRssByteSize(int maxRssByteSize)
	{
		this.maxRssByteSize = maxRssByteSize;
	}

	public long getRssCacheTimeout()
	{
		return rssCacheTimeout;
	}

	public void setRssCacheTimeout(long rssCacheTimeout)
	{
		this.rssCacheTimeout = rssCacheTimeout;
	}
}
