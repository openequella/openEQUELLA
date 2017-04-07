package com.tle.web.sections;

import com.tle.web.sections.generic.InfoBookmark;

/**
 * An abstraction of a URI which allows complicated processing to be delayed
 * until really needed.
 * <p>
 * The URI is not necessarily a full URL, it might be relative.
 * 
 * @see InfoBookmark
 * @author jmaginnis
 */
public interface Bookmark
{
	/**
	 * Get the URI.
	 * 
	 * @return The URI
	 */
	String getHref();
}
