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
