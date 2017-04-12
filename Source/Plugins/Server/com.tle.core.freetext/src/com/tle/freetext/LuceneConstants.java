package com.tle.freetext;

import org.apache.lucene.util.Version;

public final class LuceneConstants
{
	/**
	 * As most Lucene methods now require a version to be passed in and
	 * Version.LUCENE_CURRENT is now deprecated this allows us to change the
	 * version in one simple location
	 */
	public static final Version LATEST_VERSION = Version.LUCENE_35;

	// Noli me tangere constructor, because Sonar likes it that way for
	// non-instantiated utility classes
	protected LuceneConstants()
	{
		// not to be instantiated, hence nothing to construct except a token
		// hidden constructor to silence Sonar
	}
}
